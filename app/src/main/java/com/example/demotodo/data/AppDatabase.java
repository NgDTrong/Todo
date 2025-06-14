package com.example.demotodo.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.demotodo.DAO.NotificationDAO;
import com.example.demotodo.DAO.TodoDAO;
import com.example.demotodo.DAO.UserDao;
import com.example.demotodo.model.ByteArrayListConverter;
import com.example.demotodo.model.Notification;
import com.example.demotodo.model.Todo;
import com.example.demotodo.model.User;

@Database(entities = {User.class, Todo.class, Notification.class}, version = 2, exportSchema = false)
@TypeConverters({ByteArrayListConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase INSTANCE;

    public abstract UserDao userDao();
    public abstract TodoDAO todoDAO();
    public abstract NotificationDAO notificationDAO();

    public static AppDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "user_database"
                    )
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries() // Chỉ dùng để học
                    .build();
        }
        return INSTANCE;
    }
}