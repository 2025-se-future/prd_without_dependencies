import mongoose, { Schema } from 'mongoose';
import { z } from 'zod';

import { createUserSchema, GoogleUserInfo, IUser } from '../types/user.types';

const userSchema = new Schema<IUser>(
  {
    googleId: {
      type: String,
      required: true,
      unique: true,
      index: true,
    },
    email: {
      type: String,
      required: true,
      unique: true,
      lowercase: true,
      trim: true,
    },
    name: {
      type: String,
      required: true,
      trim: true,
    },
  },
  {
    timestamps: true,
  }
);

export class UserModel {
  private user: mongoose.Model<IUser>;

  constructor() {
    this.user = mongoose.model<IUser>('User', userSchema);
  }

  async create(userInfo: GoogleUserInfo): Promise<IUser> {
    try {
      const validatedData = createUserSchema.parse(userInfo);

      return await this.user.create(validatedData);
    } catch (error) {
      if (error instanceof z.ZodError) {
        console.error('Validation error:', error.issues);
        throw new Error('Invalid update data');
      }
      console.error('Error updating user:', error);
      throw new Error('Failed to update user');
    }
  }

  async findById(_id: mongoose.Types.ObjectId): Promise<IUser | null> {
    try {
      const user = await this.user.findOne({ _id });

      if (!user) {
        return null;
      }

      return user;
    } catch (error) {
      console.error('Error finding user by Google ID:', error);
      throw new Error('Failed to find user');
    }
  }

  async findByGoogleId(googleId: string): Promise<IUser | null> {
    try {
      const user = await this.user.findOne({ googleId });

      if (!user) {
        return null;
      }

      return user;
    } catch (error) {
      console.error('Error finding user by Google ID:', error);
      throw new Error('Failed to find user');
    }
  }
}

export const userModel = new UserModel();
