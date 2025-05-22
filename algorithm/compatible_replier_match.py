import numpy as np
from flask import Flask, request, jsonify
from sentence_transformers import SentenceTransformer
from sklearn.metrics.pairwise import cosine_similarity
import pymysql

app = Flask(__name__)

embedding_model = SentenceTransformer('uer/sbert-base-chinese-nli')

# MySQL 配置
DB_CONFIG = {
    'host': 'localhost',
    'user': 'root',
    'password': 'xxxxxx', #你的数据库密码
    'database': 'hangzd',
    'port': 3306
}


def weighted_user_embedding(user, tag_weight=0.7, history_weight=0.3):
    tag_vec = 0
    history_vec = 0
    print(user["tags"], user["history_replies"])
    if len(user["tags"]) > 0:
        tag_vec = embedding_model.encode(user["tags"])
    if len(user["history_replies"]) > 0:
        history_vec = embedding_model.encode(user["history_replies"])
    return tag_weight * tag_vec + history_weight * history_vec


def compute_activity_score(user):
    return (user["active_days"] / 30.0) * 0.5 + (user["like_count"] / 20.0) * 0.5


def load_users_from_mysql(sender_username):
    connection = pymysql.connect(**DB_CONFIG)
    cursor = connection.cursor(pymysql.cursors.DictCursor)

    cursor.execute(f"""
            SELECT id, active_days, like_count, avatar, username, useful_count, tags
            FROM user
            WHERE user_type = 'user' AND username != '{sender_username}'
            """)
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
    username = request.headers.get("username", "")
    question = data.get("question", "")
    if not question:
        return jsonify({"error": "Missing question"}), 400

    q_vec = embedding_model.encode(question)
    users = load_users_from_mysql(username)
    scored_users = []

    for user in users:
        if len(user["tags"]) == 0 and len(user["history_replies"]) == 0:
            final_score = 0
        else:
            user_vec = weighted_user_embedding(user)
            sim_score = cosine_similarity(
                np.atleast_2d(q_vec),
                np.atleast_2d(user_vec)
            )[0][0]
            act_score = compute_activity_score(user)
            print(sim_score)
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