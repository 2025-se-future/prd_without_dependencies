import { Router } from 'express';

import { AuthController } from '../controllers/auth.controller';
import { validateBody } from '../middleware/validation.middleware';
import {
  AuthenticateUserRequest,
  authenticateUserSchema,
} from '../types/auth.types';

const router = Router();
const authController = new AuthController();

router.post(
  '/',
  validateBody<AuthenticateUserRequest>(authenticateUserSchema),
  authController.authenticateWithGoogle
);

export default router;
