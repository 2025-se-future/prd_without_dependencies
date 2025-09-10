import dotenv from 'dotenv';
import express from 'express';

import { connectDB } from './config/database';
import {
  errorHandler,
  notFoundHandler,
} from './middleware/errorHandler.middleware';
import router from './routes';

dotenv.config();

const app = express();
const PORT = process.env.PORT ?? 3000;

app.use(express.json());

app.use('/api', router);
app.use('*', notFoundHandler);
app.use(errorHandler);

connectDB();
app.listen(PORT, () => {
  console.log(`🚀 Server running on port ${PORT}`);
});
