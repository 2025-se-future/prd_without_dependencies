import jwt from 'jsonwebtoken';
import { IUser } from '../types/user.types';

export const generateAccessToken = (user: IUser): string => {
  return jwt.sign(user._id.toString(), process.env.JWT_SECRET!);
};
