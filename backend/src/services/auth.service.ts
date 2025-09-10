import { OAuth2Client } from 'google-auth-library';

import { userModel } from '../models/user.model';
import type { AuthResult } from '../types/auth.types';
import type { GoogleUserInfo } from '../types/user.types';
import { generateAccessToken } from '../utils/auth';
import logger from '../utils/logger';

export class AuthService {
  private googleClient: OAuth2Client;

  constructor() {
    this.googleClient = new OAuth2Client(process.env.GOOGLE_CLIENT_ID);
  }

  private async verifyGoogleToken(idToken: string): Promise<GoogleUserInfo> {
    try {
      const ticket = await this.googleClient.verifyIdToken({
        idToken,
        audience: process.env.GOOGLE_CLIENT_ID,
      });

      const payload = ticket.getPayload();
      if (!payload) {
        throw new Error('Invalid token payload');
      }

      if (!payload.email || !payload.name) {
        throw new Error('Missing required user information from Google');
      }

      return {
        googleId: payload.sub,
        email: payload.email,
        name: payload.name,
      };
    } catch (error) {
      logger.error('Google token verification failed:', error);
      throw new Error('Invalid Google token');
    }
  }

  async authenticateWithGoogle(idToken: string): Promise<AuthResult> {
    try {
      const googleUserInfo = await this.verifyGoogleToken(idToken);

      let user = await userModel.findByGoogleId(googleUserInfo.googleId);
      if (!user) {
        user = await userModel.create(googleUserInfo);
      }

      const token = generateAccessToken(user);

      return { token, user };
    } catch (error) {
      logger.error('Authentication failed:', error);
      throw error;
    }
  }
}

export const authService = new AuthService();
