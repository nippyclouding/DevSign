import express from 'express';
import { createServer } from 'http';
import { WebSocketServer, WebSocket } from 'ws';
import { createServer as createViteServer } from 'vite';
import Database from 'better-sqlite3';
import bcrypt from 'bcryptjs';
import jwt from 'jsonwebtoken';
import dotenv from 'dotenv';
import path from 'path';
import { fileURLToPath } from 'url';

dotenv.config();

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const db = new Database('community.db');
const JWT_SECRET = process.env.JWT_SECRET || 'dev-secret-key';

// Initialize Database
db.exec(`
  CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    email TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    name TEXT NOT NULL,
    role TEXT NOT NULL, -- 'designer' or 'developer'
    reputation INTEGER DEFAULT 0,
    profile_data TEXT -- JSON string for portfolio/bio
  );

  CREATE TABLE IF NOT EXISTS posts (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    author_id INTEGER NOT NULL,
    main_title TEXT NOT NULL,
    subtitle TEXT NOT NULL,
    start_date TEXT NOT NULL,
    end_date TEXT NOT NULL,
    content TEXT NOT NULL,
    status TEXT DEFAULT 'recruiting', -- 'recruiting', 'progress', 'completed'
    needed_developers INTEGER DEFAULT 0,
    needed_designers INTEGER DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (author_id) REFERENCES users(id)
  );

  CREATE TABLE IF NOT EXISTS reviews (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    post_id INTEGER NOT NULL,
    reviewer_id INTEGER NOT NULL,
    reviewee_id INTEGER NOT NULL,
    content TEXT NOT NULL,
    rating INTEGER DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES posts(id),
    FOREIGN KEY (reviewer_id) REFERENCES users(id),
    FOREIGN KEY (reviewee_id) REFERENCES users(id)
  );

  CREATE TABLE IF NOT EXISTS applications (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    post_id INTEGER NOT NULL,
    applicant_id INTEGER NOT NULL,
    status TEXT DEFAULT 'pending', -- 'pending', 'approved', 'rejected'
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES posts(id),
    FOREIGN KEY (applicant_id) REFERENCES users(id)
  );

  CREATE TABLE IF NOT EXISTS messages (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    post_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    content TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES posts(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
  );
`);

// Seed Data
const seedData = () => {
  const userCount = db.prepare('SELECT COUNT(*) as count FROM users').get() as any;
  if (userCount.count === 0) {
    const hashedPassword = bcrypt.hashSync('password123', 10);
    
    // Create Users
    const insertUser = db.prepare('INSERT INTO users (email, password, name, role, reputation, profile_data) VALUES (?, ?, ?, ?, ?, ?)');
    insertUser.run('designer1@example.com', hashedPassword, 'Kim Designer', 'designer', 150, 'UI/UX Designer with 3 years of experience. Specialized in mobile apps.');
    insertUser.run('dev1@example.com', hashedPassword, 'Lee Developer', 'developer', 200, 'Full-stack developer. React, Node.js, and Python expert.');
    insertUser.run('designer2@example.com', hashedPassword, 'Park Creative', 'designer', 80, 'Graphic designer transitioning to product design.');

    // Create Posts
    const insertPost = db.prepare('INSERT INTO posts (author_id, main_title, subtitle, start_date, end_date, content, status, needed_developers, needed_designers) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)');
    insertPost.run(1, 'Entropic / Maeil Business', 'Looking for hackathon participants', '2026-03-01', '2026-03-15', 'We are looking for a developer to join our team for the upcoming AI hackathon.', 'recruiting', 2, 1);
    insertPost.run(2, 'Google Solution Challenge', 'GDSC Portfolio Project', '2026-03-10', '2026-04-20', 'Seeking a designer to help with the UI for our social impact app.', 'progress', 1, 1);
    insertPost.run(1, 'Naver D2SF', 'Startup Competition Team', '2026-04-01', '2026-05-30', 'Building a new platform for creative professionals.', 'completed', 3, 2);

    // Create some reviews for the completed project
    const insertReview = db.prepare('INSERT INTO reviews (post_id, reviewer_id, reviewee_id, content, rating) VALUES (?, ?, ?, ?, ?)');
    insertReview.run(3, 2, 1, 'Great leader! Very organized and clear communication.', 5);

    // Create some approved applications (Participants)
    const insertApp = db.prepare('INSERT INTO applications (post_id, applicant_id, status) VALUES (?, ?, ?)');
    insertApp.run(1, 2, 'approved'); // Lee Developer joined Kim Designer's project
    insertApp.run(1, 3, 'pending');  // Park Creative applied to Kim Designer's project
    insertApp.run(2, 1, 'approved'); // Kim Designer joined Lee Developer's project
  }
};
seedData();

async function startServer() {
  const app = express();
  const server = createServer(app);
  const wss = new WebSocketServer({ server });

  app.use(express.json());

  // Auth Middleware
  const authenticate = (req: any, res: any, next: any) => {
    const token = req.headers.authorization?.split(' ')[1];
    if (!token) return res.status(401).json({ error: 'Unauthorized' });
    try {
      req.user = jwt.verify(token, JWT_SECRET);
      next();
    } catch (err) {
      res.status(401).json({ error: 'Invalid token' });
    }
  };

  // Auth Routes
  app.post('/api/auth/signup', async (req, res) => {
    const { email, password, name, role } = req.body;
    try {
      const hashedPassword = await bcrypt.hash(password, 10);
      const stmt = db.prepare('INSERT INTO users (email, password, name, role) VALUES (?, ?, ?, ?)');
      const result = stmt.run(email, hashedPassword, name, role);
      const token = jwt.sign({ id: result.lastInsertRowid, email, name, role }, JWT_SECRET);
      res.json({ token, user: { id: result.lastInsertRowid, email, name, role, reputation: 0 } });
    } catch (err: any) {
      res.status(400).json({ error: err.message });
    }
  });

  app.post('/api/auth/login', async (req, res) => {
    const { email, password } = req.body;
    const user: any = db.prepare('SELECT * FROM users WHERE email = ?').get(email);
    if (user && await bcrypt.compare(password, user.password)) {
      const token = jwt.sign({ id: user.id, email: user.email, name: user.name, role: user.role }, JWT_SECRET);
      res.json({ token, user: { id: user.id, email: user.email, name: user.name, role: user.role, reputation: user.reputation, profile_data: user.profile_data } });
    } else {
      res.status(401).json({ error: 'Invalid credentials' });
    }
  });

  app.get('/api/auth/me', authenticate, (req: any, res) => {
    const user: any = db.prepare('SELECT id, email, name, role, reputation, profile_data FROM users WHERE id = ?').get(req.user.id);
    res.json(user);
  });

  app.get('/api/users/:id', (req, res) => {
    const user: any = db.prepare('SELECT id, name, role, reputation, profile_data FROM users WHERE id = ?').get(req.params.id);
    if (!user) return res.status(404).json({ error: 'User not found' });
    res.json(user);
  });

  app.put('/api/auth/profile', authenticate, (req: any, res) => {
    const { name, profile_data } = req.body;
    db.prepare('UPDATE users SET name = ?, profile_data = ? WHERE id = ?').run(name, profile_data, req.user.id);
    res.json({ success: true });
  });

  app.delete('/api/auth/account', authenticate, (req: any, res) => {
    db.prepare('DELETE FROM users WHERE id = ?').run(req.user.id);
    res.json({ success: true });
  });

  // Post Routes
  app.get('/api/posts', (req, res) => {
    const page = parseInt(req.query.page as string) || 1;
    const status = req.query.status as string;
    const limit = 10;
    const offset = (page - 1) * limit;
    
    let query = `
      SELECT p.*, u.name as author_name, u.role as author_role 
      FROM posts p 
      JOIN users u ON p.author_id = u.id 
    `;
    let countQuery = 'SELECT COUNT(*) as count FROM posts';
    const params: any[] = [];

    if (status && status !== 'all') {
      query += ' WHERE p.status = ?';
      countQuery += ' WHERE status = ?';
      params.push(status);
    }

    query += ' ORDER BY p.created_at DESC LIMIT ? OFFSET ?';
    const posts = db.prepare(query).all(...params, limit, offset);
    const total = db.prepare(countQuery).get(...params) as any;
    res.json({ posts, total: total.count, pages: Math.ceil(total.count / limit) });
  });

  app.post('/api/posts/:id/status', authenticate, (req: any, res) => {
    const { status } = req.body;
    const post: any = db.prepare('SELECT author_id FROM posts WHERE id = ?').get(req.params.id);
    if (!post || post.author_id !== req.user.id) return res.status(403).json({ error: 'Forbidden' });

    db.prepare('UPDATE posts SET status = ? WHERE id = ?').run(status, req.params.id);
    res.json({ success: true });
  });

  app.post('/api/posts', authenticate, (req: any, res) => {
    const { main_title, subtitle, start_date, end_date, content, needed_developers, needed_designers } = req.body;
    const result = db.prepare(`
      INSERT INTO posts (author_id, main_title, subtitle, start_date, end_date, content, needed_developers, needed_designers) 
      VALUES (?, ?, ?, ?, ?, ?, ?, ?)
    `).run(req.user.id, main_title, subtitle, start_date, end_date, content, needed_developers || 0, needed_designers || 0);
    res.json({ id: result.lastInsertRowid });
  });

  app.get('/api/posts/:id', (req, res) => {
    const post = db.prepare(`
      SELECT p.*, u.name as author_name, u.role as author_role 
      FROM posts p 
      JOIN users u ON p.author_id = u.id 
      WHERE p.id = ?
    `).get(req.params.id);
    if (!post) return res.status(404).json({ error: 'Post not found' });
    res.json(post);
  });

  // Application Routes
  app.post('/api/posts/:id/apply', authenticate, (req: any, res) => {
    const postId = req.params.id;
    const existing = db.prepare('SELECT * FROM applications WHERE post_id = ? AND applicant_id = ?').get(postId, req.user.id);
    if (existing) return res.status(400).json({ error: 'Already applied' });
    
    db.prepare('INSERT INTO applications (post_id, applicant_id) VALUES (?, ?)').run(postId, req.user.id);
    res.json({ success: true });
  });

  app.get('/api/posts/:id/applications', authenticate, (req: any, res) => {
    const postId = req.params.id;
    const post: any = db.prepare('SELECT author_id FROM posts WHERE id = ?').get(postId);
    const userApp: any = db.prepare('SELECT status FROM applications WHERE post_id = ? AND applicant_id = ?').get(postId, req.user.id);
    
    const isAuthor = post && post.author_id === req.user.id;
    const isApproved = userApp && userApp.status === 'approved';

    if (!isAuthor && !isApproved) {
      return res.status(403).json({ error: 'Forbidden' });
    }

    let query = `
      SELECT a.*, u.name as applicant_name, u.role as applicant_role, u.reputation as applicant_reputation, u.profile_data as applicant_profile
      FROM applications a
      JOIN users u ON a.applicant_id = u.id
      WHERE a.post_id = ?
    `;

    // If not author, only show approved ones
    if (!isAuthor) {
      query += " AND a.status = 'approved'";
    }

    const applications = db.prepare(query).all(postId);
    res.json(applications);
  });

  app.post('/api/applications/:id/status', authenticate, (req: any, res) => {
    const { status } = req.body;
    const application: any = db.prepare(`
      SELECT a.*, p.author_id 
      FROM applications a 
      JOIN posts p ON a.post_id = p.id 
      WHERE a.id = ?
    `).get(req.params.id);

    if (!application || application.author_id !== req.user.id) return res.status(403).json({ error: 'Forbidden' });

    db.prepare('UPDATE applications SET status = ? WHERE id = ?').run(status, req.params.id);
    res.json({ success: true });
  });

  // Chat History
  app.get('/api/posts/:id/messages', authenticate, (req: any, res) => {
    const postId = req.params.id;
    // Check if user is author or approved applicant
    const post: any = db.prepare('SELECT author_id FROM posts WHERE id = ?').get(postId);
    const application: any = db.prepare('SELECT status FROM applications WHERE post_id = ? AND applicant_id = ?').get(postId, req.user.id);
    
    if (post.author_id !== req.user.id && (!application || application.status !== 'approved')) {
      return res.status(403).json({ error: 'Access denied to chat' });
    }

    const messages = db.prepare(`
      SELECT m.*, u.name as user_name, u.role as user_role
      FROM messages m
      JOIN users u ON m.user_id = u.id
      WHERE m.post_id = ?
      ORDER BY m.created_at ASC
    `).all(postId);
    res.json(messages);
  });

  // User Projects & Reviews
  app.get('/api/users/projects', authenticate, (req: any, res) => {
    const created = db.prepare('SELECT * FROM posts WHERE author_id = ?').all(req.user.id);
    const joined = db.prepare(`
      SELECT p.* 
      FROM posts p 
      JOIN applications a ON p.id = a.post_id 
      WHERE a.applicant_id = ? AND a.status = 'approved'
    `).all(req.user.id);
    res.json({ created, joined });
  });

  app.get('/api/users/:id/reviews', (req, res) => {
    const reviews = db.prepare(`
      SELECT r.*, u.name as reviewer_name, p.main_title as project_title
      FROM reviews r
      JOIN users u ON r.reviewer_id = u.id
      JOIN posts p ON r.post_id = p.id
      WHERE r.reviewee_id = ?
    `).all(req.params.id);
    res.json(reviews);
  });

  app.post('/api/reviews', authenticate, (req: any, res) => {
    const { post_id, reviewee_id, content, rating } = req.body;
    
    // Verify post is completed
    const post: any = db.prepare('SELECT status FROM posts WHERE id = ?').get(post_id);
    if (!post || post.status !== 'completed') return res.status(400).json({ error: 'Project not completed' });

    db.prepare('INSERT INTO reviews (post_id, reviewer_id, reviewee_id, content, rating) VALUES (?, ?, ?, ?, ?)')
      .run(post_id, req.user.id, reviewee_id, content, rating);
    
    // Update user reputation
    db.prepare('UPDATE users SET reputation = reputation + ? WHERE id = ?').run(rating, reviewee_id);
    
    res.json({ success: true });
  });

  // WebSocket Chat
  const clients = new Map<number, Set<WebSocket>>();

  wss.on('connection', (ws, req) => {
    let currentPostId: number | null = null;
    let currentUser: any = null;

    ws.on('message', async (data) => {
      const message = JSON.parse(data.toString());

      if (message.type === 'join') {
        const { token, postId } = message;
        try {
          const decoded: any = jwt.verify(token, JWT_SECRET);
          currentUser = decoded;
          currentPostId = postId;

          if (!clients.has(postId)) clients.set(postId, new Set());
          clients.get(postId)!.add(ws);
        } catch (e) {
          ws.close();
        }
      } else if (message.type === 'chat') {
        if (!currentPostId || !currentUser) return;

        const stmt = db.prepare('INSERT INTO messages (post_id, user_id, content) VALUES (?, ?, ?)');
        const result = stmt.run(currentPostId, currentUser.id, message.content);
        
        const chatMsg = {
          type: 'chat',
          id: result.lastInsertRowid,
          post_id: currentPostId,
          user_id: currentUser.id,
          user_name: currentUser.name,
          user_role: currentUser.role,
          content: message.content,
          created_at: new Date().toISOString()
        };

        const roomClients = clients.get(currentPostId);
        if (roomClients) {
          roomClients.forEach(client => {
            if (client.readyState === WebSocket.OPEN) {
              client.send(JSON.stringify(chatMsg));
            }
          });
        }
      }
    });

    ws.on('close', () => {
      if (currentPostId && clients.has(currentPostId)) {
        clients.get(currentPostId)!.delete(ws);
      }
    });
  });

  // Vite for Frontend
  if (process.env.NODE_ENV !== 'production') {
    const vite = await createViteServer({
      server: { middlewareMode: true },
      appType: 'spa',
    });
    app.use(vite.middlewares);
  } else {
    app.use(express.static(path.join(__dirname, 'dist')));
    app.get('*', (req, res) => {
      res.sendFile(path.join(__dirname, 'dist', 'index.html'));
    });
  }

  const PORT = 3000;
  server.listen(PORT, '0.0.0.0', () => {
    console.log(`Server running on http://localhost:${PORT}`);
  });
}

startServer();
