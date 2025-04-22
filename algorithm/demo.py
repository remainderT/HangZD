from sentence_transformers import SentenceTransformer
from sklearn.metrics.pairwise import cosine_similarity
import re

embedding_model = SentenceTransformer('multi-qa-mpnet-base-dot-v1')

users = [
    {
        "user_id": "user1",
        "tags": ["深度学习", "计算机视觉"],
        "history": ["如何训练图像分类模型", "ResNet与VGG的比较"],
        "active_days": 1,
        "avg_likes": 1
    },
    {
        "user_id": "user2",
        "tags": ["JavaScript", "前端开发"],
        "history": ["Vue和React谁更好", "HTML语义化标签"],
        "active_days": 1,
        "avg_likes": 1
    },
    {
        "user_id": "user3",
        "tags": ["数据分析", "Python"],
        "history": ["Pandas数据清洗技巧", "数据预处理方法"],
        "active_days": 1,
        "avg_likes": 1
    },
    {
        "user_id": "user4",
        "tags": ["自然语言处理", "机器翻译"],
        "history": ["BERT模型原理", "Transformer如何进行翻译"],
        "active_days": 1,
        "avg_likes": 1
    },
    {
        "user_id": "user5",
        "tags": ["后端开发", "数据库设计"],
        "history": ["MySQL索引优化", "数据库范式讲解"],
        "active_days": 1,
        "avg_likes": 1
    },
    {
        "user_id": "user6",
        "tags": ["强化学习", "智能体建模"],
        "history": ["Q-learning与SARSA的区别", "Gym环境的使用方法"],
        "active_days": 1,
        "avg_likes": 1
    },
    {
        "user_id": "user7",
        "tags": ["大数据", "Hadoop", "Spark"],
        "history": ["Hadoop集群搭建经验", "Spark数据处理实战"],
        "active_days": 1,
        "avg_likes": 1
    },
    {
        "user_id": "user8",
        "tags": ["iOS开发", "Swift"],
        "history": ["SwiftUI布局指南", "Xcode调试技巧"],
        "active_days": 1,
        "avg_likes": 1
    },
    {
        "user_id": "user9",
        "tags": ["机器学习", "模型调参"],
        "history": ["交叉验证的原理", "LightGBM调参技巧"],
        "active_days": 1,
        "avg_likes": 1
    },
    {
        "user_id": "user10",
        "tags": ["区块链", "智能合约"],
        "history": ["Solidity入门教程", "以太坊Gas机制详解"],
        "active_days": 1,
        "avg_likes": 1
    }
]
from transformers import MarianMTModel, MarianTokenizer

# 加载模型和分词器（只需加载一次）
model_name = 'Helsinki-NLP/opus-mt-zh-en'
tokenizer = MarianTokenizer.from_pretrained(model_name)
model = MarianMTModel.from_pretrained(model_name)


def translate_zh_to_en(text):
    """
    将中文翻译成英文
    参数:
        text: str，中文内容
    返回:
        str，英文翻译结果
    """
    inputs = tokenizer(text, return_tensors="pt", padding=True, truncation=True)
    translated = model.generate(**inputs)
    return tokenizer.decode(translated[0], skip_special_tokens=True)


def weighted_user_embedding(user, tag_weight=0.7, history_weight=0.3):
    tag_text = ' '.join(user["tags"])
    history_text = ' '.join(user["history"])

    tag_text = translate_zh_to_en(tag_text)
    history_text = translate_zh_to_en(history_text)
    # print(tag_text, history_text)
    tag_vec = embedding_model.encode(tag_text)
    history_vec = embedding_model.encode(history_text)

    return tag_weight * tag_vec + history_weight * history_vec

def compute_activity_score(user):
    activity_score = (user["active_days"] / 30.0) * 0.5 + (user["avg_likes"] / 20.0) * 0.5
    return activity_score


def extract_first_number(text):
    match = re.search(r'\d+', text)
    if match:
        return int(match.group())
    return None


def recommend_answerers_for_question(question_text, top_n=2):
    q_vec = embedding_model.encode(question_text)
    scores = []

    for user in users:
        user_vec = weighted_user_embedding(user)
        sim_score = cosine_similarity([q_vec], [user_vec])[0][0]
        act_score = compute_activity_score(user)

        final_score = sim_score * act_score
        scores.append((user["user_id"], final_score))

    scores.sort(key=lambda x: x[1], reverse=True)
    return scores[:top_n]


if __name__ == "__main__":
    query = ("怎么更好的对数据进行分析")
    query = translate_zh_to_en(query)
    print(query)
    results = recommend_answerers_for_question(query, top_n=10)

    print("\n推荐结果：")
    for uid, score in results:
        print(f"推荐用户：{uid}", users[extract_first_number(uid)-1])

