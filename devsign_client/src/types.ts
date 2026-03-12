export type Role = 'designer' | 'developer';

export interface User {
  id: number;
  email: string;
  name: string;
  role: Role;
  reputation: number;
  profile_data?: string;
}

export interface Post {
  id: number;
  author_id: number;
  author_name: string;
  author_role: Role;
  author_reputation: number;
  main_title: string;
  subtitle: string;
  start_date: string;
  end_date: string;
  content?: string;
  status: 'recruiting' | 'progress' | 'completed';
  needed_developers: number;
  needed_designers: number;
  group_chat_id?: number;
  created_at: string;
}

export interface Review {
  id: number;
  post_id: number;
  project_title: string;
  reviewer_id: number;
  reviewer_name: string;
  reviewee_id: number;
  content: string;
  rating: number;
  created_at: string;
}

export interface Application {
  id: number;
  post_id: number;
  applicant_id: number;
  applicant_name: string;
  applicant_role: Role;
  applicant_reputation: number;
  applicant_profile?: string;
  status: 'pending' | 'approved' | 'rejected';
  created_at: string;
}

export interface Message {
  id: number;
  user_id: number;
  user_name: string;
  user_role: Role;
  content: string;
  created_at: string;
}

export interface ProjectStats {
  active_projects: number;
  today_projects: number;
}

export interface Membership {
  is_author: boolean;
  is_approved: boolean;
  application_status: 'pending' | 'approved' | 'rejected' | null;
  application_id: number | null;
}

export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message: string;
}
