import { getAllTasks, createTask, updateTask, deleteTask } from "../services/task-service.js";

export const getTasks = async (req, res) => {
    try {
        // req.user viene del token verificado en auth-mid.js
        const tasks = await getAllTasks(req.user.id);
        res.json(tasks);
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
};

export const create = async (req, res) => {
    try {
        const { title, description } = req.body;
        const newTask = await createTask(req.user.id, title, description);
        res.status(201).json(newTask);
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
};

export const update = async (req, res) => {
    try {
        const { title, description, is_completed } = req.body;
        const updatedTask = await updateTask(req.user.id, req.params.id, title, description, is_completed);
        res.json(updatedTask);
    } catch (error) {
        if (error.message === "TASK_NOT_FOUND") {
            return res.status(404).json({ message: "Tarea no encontrada o no autorizada" });
        }
        res.status(500).json({ error: error.message });
    }
};

export const remove = async (req, res) => {
    try {
        await deleteTask(req.user.id, req.params.id);
        res.json({ message: "Tarea eliminada correctamente" });
    } catch (error) {
        if (error.message === "TASK_NOT_FOUND") {
            return res.status(404).json({ message: "Tarea no encontrada o no autorizada" });
        }
        res.status(500).json({ error: error.message });
    }
};