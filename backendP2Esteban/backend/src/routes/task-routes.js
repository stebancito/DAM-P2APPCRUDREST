import express from "express";
import { getTasks, create, update, remove } from "../controllers/task-controller.js";
import { verifyToken } from "../middlewares/auth-mid.js";

const router = express.Router();

// Middleware aplicado a TODAS las rutas
router.use(verifyToken);

// Rutas CRUD
router.get("/", getTasks);         
router.post("/", create);      
router.put("/:id", update);        
router.delete("/:id", remove);      

export default router;