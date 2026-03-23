import { registerUser, loginUser } from "../services/auth-service.js";

export const register = async (req, res) => {

    try {

        const { username, password } = req.body;

        await registerUser(username, password);

        res.status(201).json({
            message: "Usuario registrado correctamente"
        });

    } catch (error) {
        console.error("Error en registro de usuario:", error.message);
        if (error.message === "USER_EXISTS") {
            return res.status(400).json({ message: "El usuario ya existe" });
        }

        res.status(500).json({ error: error.message });
    }
};


export const login = async (req, res) => {

    try {

        const { username, password } = req.body;

        const token = await loginUser(username, password);

        res.json({
            message: "Login exitoso",
            token
        });

    } catch (error) {
        console.error("Error en inicio de sesión:", error.message);

        if (error.message === "USER_NOT_FOUND") {
            return res.status(404).json({ message: "Usuario no encontrado" });
        }

        if (error.message === "INVALID_PASSWORD") {
            return res.status(401).json({ message: "Contraseña incorrecta" });
        }

        res.status(500).json({ error: error.message });
    }
};