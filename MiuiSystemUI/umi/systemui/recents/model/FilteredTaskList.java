package com.android.systemui.recents.model;

import android.util.ArrayMap;
import android.util.SparseArray;
import com.android.systemui.recents.model.Task;
import java.util.ArrayList;
import java.util.List;

/* compiled from: TaskStack */
class FilteredTaskList {
    TaskFilter mFilter;
    ArrayList<Task> mFilteredTasks = new ArrayList<>();
    ArrayMap<Task.TaskKey, Integer> mTaskIndices = new ArrayMap<>();
    ArrayList<Task> mTasks = new ArrayList<>();

    FilteredTaskList() {
    }

    /* access modifiers changed from: package-private */
    public boolean setFilter(TaskFilter taskFilter) {
        ArrayList arrayList = new ArrayList(this.mFilteredTasks);
        this.mFilter = taskFilter;
        updateFilteredTasks();
        return !arrayList.equals(this.mFilteredTasks);
    }

    public void moveTaskToStack(Task task, int i, int i2) {
        int indexOf = indexOf(task);
        if (indexOf != i) {
            this.mTasks.remove(indexOf);
            if (indexOf < i) {
                i--;
            }
            this.mTasks.add(i, task);
        }
        task.setStackId(i2);
        updateFilteredTasks();
    }

    /* access modifiers changed from: package-private */
    public void set(List<Task> list) {
        this.mTasks.clear();
        this.mTasks.addAll(list);
        updateFilteredTasks();
    }

    /* access modifiers changed from: package-private */
    public boolean remove(Task task) {
        if (!this.mFilteredTasks.contains(task)) {
            return false;
        }
        boolean remove = this.mTasks.remove(task);
        updateFilteredTasks();
        return remove;
    }

    /* access modifiers changed from: package-private */
    public int indexOf(Task task) {
        if (task == null || !this.mTaskIndices.containsKey(task.key)) {
            return -1;
        }
        return this.mTaskIndices.get(task.key).intValue();
    }

    /* access modifiers changed from: package-private */
    public int size() {
        return this.mFilteredTasks.size();
    }

    /* access modifiers changed from: package-private */
    public boolean contains(Task task) {
        return this.mTaskIndices.containsKey(task.key);
    }

    private void updateFilteredTasks() {
        this.mFilteredTasks.clear();
        if (this.mFilter != null) {
            SparseArray sparseArray = new SparseArray();
            int size = this.mTasks.size();
            for (int i = 0; i < size; i++) {
                Task task = this.mTasks.get(i);
                sparseArray.put(task.key.id, task);
            }
            for (int i2 = 0; i2 < size; i2++) {
                Task task2 = this.mTasks.get(i2);
                if (this.mFilter.acceptTask(sparseArray, task2, i2)) {
                    this.mFilteredTasks.add(task2);
                }
            }
        } else {
            this.mFilteredTasks.addAll(this.mTasks);
        }
        updateFilteredTaskIndices();
    }

    private void updateFilteredTaskIndices() {
        int size = this.mFilteredTasks.size();
        this.mTaskIndices.clear();
        for (int i = 0; i < size; i++) {
            this.mTaskIndices.put(this.mFilteredTasks.get(i).key, Integer.valueOf(i));
        }
    }

    /* access modifiers changed from: package-private */
    public ArrayList<Task> getTasks() {
        return this.mFilteredTasks;
    }
}
