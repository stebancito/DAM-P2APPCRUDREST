import pool from "../config/db.js";


export const getAllTasks = async (userId) => {
    const result = await pool.query(
        "SELECT * FROM tasks WHERE user_id = $1 ORDER BY created_at DESC", 
        [userId]
    );
    return result.rows;
};

export const createTask = async (userId, title, description) => {
    const result = await pool.query(
        "INSERT INTO tasks (user_id, title, description) VALUES ($1, $2, $3) RETURNING *",
        [userId, title, description]
    );
    return result.rows[0];
};

export const updateTask = async (userId, taskId, title, description, isCompleted) => {
    const result = await pool.query(
        "UPDATE tasks SET title = $1, description = $2, is_completed = $3 WHERE user_id = $4 AND id = $5 RETURNING *",
        [title, description, isCompleted, userId, taskId]
    );
    
    if (result.rows.length === 0) {
        throw new Error("TASK_NOT_FOUND");
    }
    return result.rows[0];
};

export const deleteTask = async (userId, taskId) => {
    const result = await pool.query(
        "DELETE FROM tasks WHERE user_id = $1 AND id = $2 RETURNING *", 
        [userId, taskId]
    );
    
    if (result.rows.length === 0) {
        throw new Error("TASK_NOT_FOUND");
    }
    return true;
};