package com.example.gallery;

public interface TaskListener {
	public void onTaskCompleted(TaskType taskType, Object result, Object data);
}
