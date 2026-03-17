import bcrypt from "bcrypt";
import jwt from "jsonwebtoken";
import pool from "../config/db.js";

const SECRET = "supersecret";

export const registerUser = async (username, password) => {

    const userExists = await pool.query(
        "SELECT * FROM users WHERE username=$1",
        [username]
    );

    if (userExists.rows.length > 0) {
        throw new Error("USER_EXISTS");
    }

    const hash = await bcrypt.hash(password, 10);

    await pool.query(
        "INSERT INTO users(username, password_hash) VALUES($1,$2)",
        [username, hash]
    );

    return true;
};


export const loginUser = async (username, password) => {

    const result = await pool.query(
        "SELECT * FROM users WHERE username=$1",
        [username]
    );

    if (result.rows.length === 0) {
        throw new Error("USER_NOT_FOUND");
    }

    const user = result.rows[0];

    const valid = await bcrypt.compare(password, user.password_hash);

    if (!valid) {
        throw new Error("INVALID_PASSWORD");
    }

    const token = jwt.sign(
        { id: user.id, username: user.username },
        SECRET,
        { expiresIn: "1h" }
    );

    return token;
};