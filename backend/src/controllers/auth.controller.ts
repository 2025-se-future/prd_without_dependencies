import { NextFunction, Request, Response } from 'express';

import { authService } from '../services/auth.service';
import {
  AuthenticateUserRequest,
  AuthenticateUserResponse,
} from '../types/auth.types';
import logger from '../utils/logger';

export class AuthController {
  async authenticateWithGoogle(
    req: Request<unknown, unknown, AuthenticateUserRequest>,
    res: Response<AuthenticateUserResponse>,
    next: NextFunction
  ) {
    try {
      const { idToken } = req.body;

      const data = await authService.authenticateWithGoogle(idToken);

      return res.status(201).json({
        message: 'User signed up successfully',
        data,
      });
    } catch (error) {
      logger.error('Google sign up error:', error);

      if (error instanceof Error) {
        if (error.message === 'Invalid Google token') {
          return res.status(401).json({
            message: 'Invalid Google token',
          });
        }

        if (error.message === 'Failed to process user') {
          return res.status(500).json({
            message: 'Failed to process user information',
          });
        }
      }

      next(error);
    }
  }
}
