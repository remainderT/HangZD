from flask import Flask, request, jsonify
from sentence_transformers import SentenceTransformer
from sklearn.metrics.pairwise import cosine_similarity
from transformers import MarianMTModel, MarianTokenizer
import pymysql

app = Flask(__name__)

# 加载翻译模型和句向量模型
translate_model_name = 'Helsinki-NLP/opus-mt-zh-en'
tokenizer = MarianTokenizer.from_pretrained(translate_model_name)
translate_model = MarianMTModel.from_pretrained(translate_model_name)
embedding_model = SentenceTransformer('multi-qa-mpnet-base-dot-v1')

# MySQL 配置
DB_CONFIG = {
    'host': 'localhost',
    'user': 'root',
    'password': 'xxxxxx', #你的数据库密码
    'database': 'hangzd',
    'port': 3306
}


def translate_zh_to_en(text):
    inputs = tokenizer(text, return_tensors="pt", padding=True, truncation=True)
    translated = translate_model.generate(**inputs)
    return tokenizer.decode(translated[0], skip_special_tokens=True)


def weighted_user_embedding(user, tag_weight=0.7, history_weight=0.3):
    tag_vec = 0
    history_vec = 0
    if len(user["tags"]) > 0:
        tag_text = translate_zh_to_en(user["tags"])
        tag_vec = embedding_model.encode(tag_text)
    if len(user["history_replies"]) > 0:
        history_text = translate_zh_to_en(user["history_replies"])
        history_vec = embedding_model.encode(history_text)
    return tag_weight * tag_vec + history_weight * history_vec


def compute_activity_score(user):
    return (user["active_days"] / 30.0) * 0.5 + (user["like_count"] / 20.0) * 0.5


def load_users_from_mysql():
    connection = pymysql.connect(**DB_CONFIG)
    cursor = connection.cursor(pymysql.cursors.DictCursor)

    cursor.execute("SELECT id, active_days, like_count, avatar, username, useful_count, tags FROM user WHERE user_type = 'user'")
    activity_data = cursor.fetchall()
    cursor.execute("SELECT user_id, content FROM answer")
    replies = cursor.fetchall()
    users = []
    for user in activity_data:
        id = user["id"]
        like_count = user["like_count"]
        active_days = user["active_days"]
        avatar = user["avatar"]
        username = user["username"]
        useful_count = user["useful_count"]
        tags = user["tags"].split(',') if user["tags"] else []

        history_replies = []
        for reply in replies:
            if reply["user_id"] == id and reply["content"]:
                history_replies.append(reply["content"])
        users.append({
            "id": id,
            "tags": tags,
            "history_replies": history_replies,
            "active_days": active_days,
            "like_count": like_count,
            "avatar": avatar,
            "username": username,
            "useful_count": useful_count
        })

    # print(users)
    cursor.close()
    connection.close()
    return users


@app.route("/recommend", methods=["POST"])
def recommend():
    data = request.get_json()
    question = data.get("question", "")
    if not question:
        return jsonify({"error": "Missing question"}), 400

    translated_question = translate_zh_to_en(question)
    q_vec = embedding_model.encode(translated_question)
    users = load_users_from_mysql()
    scored_users = []

    for user in users:
        user_vec = weighted_user_embedding(user)
        sim_score = cosine_similarity([q_vec], [user_vec])[0][0]
        act_score = compute_activity_score(user)
        final_score = sim_score * act_score
        scored_users.append((final_score, user))

    top_users = sorted(scored_users, key=lambda x: x[0], reverse=True)[:10]
    results = [{
        "id": u["id"],
        "tags": u["tags"],
        "history_replies": u["history_replies"],
        "active_days": u["active_days"],
        "like_count": u["like_count"],
        "avatar": u["avatar"],
        "username": u["username"],
        "userful_count": u["useful_count"]
    } for _, u in top_users]

    return jsonify(results)


if __name__ == "__main__":
    app.run(debug=True, host="0.0.0.0", port=4999)