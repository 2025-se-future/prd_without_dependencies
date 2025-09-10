import mongoose, { Document } from 'mongoose';
import z from 'zod';

// User model
// ------------------------------------------------------------
export interface IUser extends Document {
  _id: mongoose.Types.ObjectId;
  googleId: string;
  email: string;
  name: string;
  createdAt: Date;
  updatedAt: Date;
}

// Zod schemas
// ------------------------------------------------------------
export const createUserSchema = z.object({
  email: z.email(),
  name: z.string().min(1),
  googleId: z.string().min(1),
});

// Generic types
// ------------------------------------------------------------
export type GoogleUserInfo = {
  googleId: string;
  email: string;
  name: string;
};
