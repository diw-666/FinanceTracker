package com.example.financetracker.database.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.lifecycle.LiveData;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.example.financetracker.database.converters.DateConverter;
import com.example.financetracker.database.converters.StringListConverter;
import com.example.financetracker.database.entities.BudgetEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.IllegalStateException;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class BudgetDao_Impl implements BudgetDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<BudgetEntity> __insertionAdapterOfBudgetEntity;

  private final DateConverter __dateConverter = new DateConverter();

  private final StringListConverter __stringListConverter = new StringListConverter();

  private final EntityDeletionOrUpdateAdapter<BudgetEntity> __deletionAdapterOfBudgetEntity;

  private final EntityDeletionOrUpdateAdapter<BudgetEntity> __updateAdapterOfBudgetEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteById;

  public BudgetDao_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfBudgetEntity = new EntityInsertionAdapter<BudgetEntity>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR REPLACE INTO `budgets` (`id`,`amount`,`startDate`,`endDate`,`warningThreshold`,`categories`) VALUES (?,?,?,?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, BudgetEntity value) {
        if (value.getId() == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindString(1, value.getId());
        }
        stmt.bindDouble(2, value.getAmount());
        final Long _tmp = __dateConverter.dateToTimestamp(value.getStartDate());
        if (_tmp == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindLong(3, _tmp);
        }
        final Long _tmp_1 = __dateConverter.dateToTimestamp(value.getEndDate());
        if (_tmp_1 == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindLong(4, _tmp_1);
        }
        stmt.bindLong(5, value.getWarningThreshold());
        final String _tmp_2 = __stringListConverter.fromStringList(value.getCategories());
        if (_tmp_2 == null) {
          stmt.bindNull(6);
        } else {
          stmt.bindString(6, _tmp_2);
        }
      }
    };
    this.__deletionAdapterOfBudgetEntity = new EntityDeletionOrUpdateAdapter<BudgetEntity>(__db) {
      @Override
      public String createQuery() {
        return "DELETE FROM `budgets` WHERE `id` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, BudgetEntity value) {
        if (value.getId() == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindString(1, value.getId());
        }
      }
    };
    this.__updateAdapterOfBudgetEntity = new EntityDeletionOrUpdateAdapter<BudgetEntity>(__db) {
      @Override
      public String createQuery() {
        return "UPDATE OR ABORT `budgets` SET `id` = ?,`amount` = ?,`startDate` = ?,`endDate` = ?,`warningThreshold` = ?,`categories` = ? WHERE `id` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, BudgetEntity value) {
        if (value.getId() == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindString(1, value.getId());
        }
        stmt.bindDouble(2, value.getAmount());
        final Long _tmp = __dateConverter.dateToTimestamp(value.getStartDate());
        if (_tmp == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindLong(3, _tmp);
        }
        final Long _tmp_1 = __dateConverter.dateToTimestamp(value.getEndDate());
        if (_tmp_1 == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindLong(4, _tmp_1);
        }
        stmt.bindLong(5, value.getWarningThreshold());
        final String _tmp_2 = __stringListConverter.fromStringList(value.getCategories());
        if (_tmp_2 == null) {
          stmt.bindNull(6);
        } else {
          stmt.bindString(6, _tmp_2);
        }
        if (value.getId() == null) {
          stmt.bindNull(7);
        } else {
          stmt.bindString(7, value.getId());
        }
      }
    };
    this.__preparedStmtOfDeleteById = new SharedSQLiteStatement(__db) {
      @Override
      public String createQuery() {
        final String _query = "DELETE FROM budgets WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final BudgetEntity budget, final Continuation<? super Unit> continuation) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfBudgetEntity.insert(budget);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, continuation);
  }

  @Override
  public Object delete(final BudgetEntity budget, final Continuation<? super Unit> continuation) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfBudgetEntity.handle(budget);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, continuation);
  }

  @Override
  public Object update(final BudgetEntity budget, final Continuation<? super Unit> continuation) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfBudgetEntity.handle(budget);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, continuation);
  }

  @Override
  public Object deleteById(final String budgetId, final Continuation<? super Unit> continuation) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteById.acquire();
        int _argIndex = 1;
        if (budgetId == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, budgetId);
        }
        __db.beginTransaction();
        try {
          _stmt.executeUpdateDelete();
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
          __preparedStmtOfDeleteById.release(_stmt);
        }
      }
    }, continuation);
  }

  @Override
  public LiveData<List<BudgetEntity>> getAllBudgets() {
    final String _sql = "SELECT `budgets`.`id` AS `id`, `budgets`.`amount` AS `amount`, `budgets`.`startDate` AS `startDate`, `budgets`.`endDate` AS `endDate`, `budgets`.`warningThreshold` AS `warningThreshold`, `budgets`.`categories` AS `categories` FROM budgets ORDER BY startDate DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[]{"budgets"}, false, new Callable<List<BudgetEntity>>() {
      @Override
      public List<BudgetEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = 0;
          final int _cursorIndexOfAmount = 1;
          final int _cursorIndexOfStartDate = 2;
          final int _cursorIndexOfEndDate = 3;
          final int _cursorIndexOfWarningThreshold = 4;
          final int _cursorIndexOfCategories = 5;
          final List<BudgetEntity> _result = new ArrayList<BudgetEntity>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final BudgetEntity _item;
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final Date _tmpStartDate;
            final Long _tmp;
            if (_cursor.isNull(_cursorIndexOfStartDate)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getLong(_cursorIndexOfStartDate);
            }
            final Date _tmp_1 = __dateConverter.fromTimestamp(_tmp);
            if(_tmp_1 == null) {
              throw new IllegalStateException("Expected non-null java.util.Date, but it was null.");
            } else {
              _tmpStartDate = _tmp_1;
            }
            final Date _tmpEndDate;
            final Long _tmp_2;
            if (_cursor.isNull(_cursorIndexOfEndDate)) {
              _tmp_2 = null;
            } else {
              _tmp_2 = _cursor.getLong(_cursorIndexOfEndDate);
            }
            final Date _tmp_3 = __dateConverter.fromTimestamp(_tmp_2);
            if(_tmp_3 == null) {
              throw new IllegalStateException("Expected non-null java.util.Date, but it was null.");
            } else {
              _tmpEndDate = _tmp_3;
            }
            final int _tmpWarningThreshold;
            _tmpWarningThreshold = _cursor.getInt(_cursorIndexOfWarningThreshold);
            final List<String> _tmpCategories;
            final String _tmp_4;
            if (_cursor.isNull(_cursorIndexOfCategories)) {
              _tmp_4 = null;
            } else {
              _tmp_4 = _cursor.getString(_cursorIndexOfCategories);
            }
            _tmpCategories = __stringListConverter.toStringList(_tmp_4);
            _item = new BudgetEntity(_tmpId,_tmpAmount,_tmpStartDate,_tmpEndDate,_tmpWarningThreshold,_tmpCategories);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getBudgetById(final String budgetId,
      final Continuation<? super BudgetEntity> continuation) {
    final String _sql = "SELECT * FROM budgets WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (budgetId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, budgetId);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<BudgetEntity>() {
      @Override
      public BudgetEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "startDate");
          final int _cursorIndexOfEndDate = CursorUtil.getColumnIndexOrThrow(_cursor, "endDate");
          final int _cursorIndexOfWarningThreshold = CursorUtil.getColumnIndexOrThrow(_cursor, "warningThreshold");
          final int _cursorIndexOfCategories = CursorUtil.getColumnIndexOrThrow(_cursor, "categories");
          final BudgetEntity _result;
          if(_cursor.moveToFirst()) {
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final Date _tmpStartDate;
            final Long _tmp;
            if (_cursor.isNull(_cursorIndexOfStartDate)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getLong(_cursorIndexOfStartDate);
            }
            final Date _tmp_1 = __dateConverter.fromTimestamp(_tmp);
            if(_tmp_1 == null) {
              throw new IllegalStateException("Expected non-null java.util.Date, but it was null.");
            } else {
              _tmpStartDate = _tmp_1;
            }
            final Date _tmpEndDate;
            final Long _tmp_2;
            if (_cursor.isNull(_cursorIndexOfEndDate)) {
              _tmp_2 = null;
            } else {
              _tmp_2 = _cursor.getLong(_cursorIndexOfEndDate);
            }
            final Date _tmp_3 = __dateConverter.fromTimestamp(_tmp_2);
            if(_tmp_3 == null) {
              throw new IllegalStateException("Expected non-null java.util.Date, but it was null.");
            } else {
              _tmpEndDate = _tmp_3;
            }
            final int _tmpWarningThreshold;
            _tmpWarningThreshold = _cursor.getInt(_cursorIndexOfWarningThreshold);
            final List<String> _tmpCategories;
            final String _tmp_4;
            if (_cursor.isNull(_cursorIndexOfCategories)) {
              _tmp_4 = null;
            } else {
              _tmp_4 = _cursor.getString(_cursorIndexOfCategories);
            }
            _tmpCategories = __stringListConverter.toStringList(_tmp_4);
            _result = new BudgetEntity(_tmpId,_tmpAmount,_tmpStartDate,_tmpEndDate,_tmpWarningThreshold,_tmpCategories);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, continuation);
  }

  @Override
  public Object getActiveBudget(final Date date,
      final Continuation<? super BudgetEntity> continuation) {
    final String _sql = "SELECT * FROM budgets WHERE ? BETWEEN startDate AND endDate";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    final Long _tmp = __dateConverter.dateToTimestamp(date);
    if (_tmp == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindLong(_argIndex, _tmp);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<BudgetEntity>() {
      @Override
      public BudgetEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "startDate");
          final int _cursorIndexOfEndDate = CursorUtil.getColumnIndexOrThrow(_cursor, "endDate");
          final int _cursorIndexOfWarningThreshold = CursorUtil.getColumnIndexOrThrow(_cursor, "warningThreshold");
          final int _cursorIndexOfCategories = CursorUtil.getColumnIndexOrThrow(_cursor, "categories");
          final BudgetEntity _result;
          if(_cursor.moveToFirst()) {
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final Date _tmpStartDate;
            final Long _tmp_1;
            if (_cursor.isNull(_cursorIndexOfStartDate)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getLong(_cursorIndexOfStartDate);
            }
            final Date _tmp_2 = __dateConverter.fromTimestamp(_tmp_1);
            if(_tmp_2 == null) {
              throw new IllegalStateException("Expected non-null java.util.Date, but it was null.");
            } else {
              _tmpStartDate = _tmp_2;
            }
            final Date _tmpEndDate;
            final Long _tmp_3;
            if (_cursor.isNull(_cursorIndexOfEndDate)) {
              _tmp_3 = null;
            } else {
              _tmp_3 = _cursor.getLong(_cursorIndexOfEndDate);
            }
            final Date _tmp_4 = __dateConverter.fromTimestamp(_tmp_3);
            if(_tmp_4 == null) {
              throw new IllegalStateException("Expected non-null java.util.Date, but it was null.");
            } else {
              _tmpEndDate = _tmp_4;
            }
            final int _tmpWarningThreshold;
            _tmpWarningThreshold = _cursor.getInt(_cursorIndexOfWarningThreshold);
            final List<String> _tmpCategories;
            final String _tmp_5;
            if (_cursor.isNull(_cursorIndexOfCategories)) {
              _tmp_5 = null;
            } else {
              _tmp_5 = _cursor.getString(_cursorIndexOfCategories);
            }
            _tmpCategories = __stringListConverter.toStringList(_tmp_5);
            _result = new BudgetEntity(_tmpId,_tmpAmount,_tmpStartDate,_tmpEndDate,_tmpWarningThreshold,_tmpCategories);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, continuation);
  }

  @Override
  public LiveData<List<BudgetEntity>> getBudgetsInPeriod(final Date startDate, final Date endDate) {
    final String _sql = "SELECT * FROM budgets WHERE startDate <= ? AND endDate >= ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    final Long _tmp = __dateConverter.dateToTimestamp(endDate);
    if (_tmp == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindLong(_argIndex, _tmp);
    }
    _argIndex = 2;
    final Long _tmp_1 = __dateConverter.dateToTimestamp(startDate);
    if (_tmp_1 == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindLong(_argIndex, _tmp_1);
    }
    return __db.getInvalidationTracker().createLiveData(new String[]{"budgets"}, false, new Callable<List<BudgetEntity>>() {
      @Override
      public List<BudgetEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "startDate");
          final int _cursorIndexOfEndDate = CursorUtil.getColumnIndexOrThrow(_cursor, "endDate");
          final int _cursorIndexOfWarningThreshold = CursorUtil.getColumnIndexOrThrow(_cursor, "warningThreshold");
          final int _cursorIndexOfCategories = CursorUtil.getColumnIndexOrThrow(_cursor, "categories");
          final List<BudgetEntity> _result = new ArrayList<BudgetEntity>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final BudgetEntity _item;
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final Date _tmpStartDate;
            final Long _tmp_2;
            if (_cursor.isNull(_cursorIndexOfStartDate)) {
              _tmp_2 = null;
            } else {
              _tmp_2 = _cursor.getLong(_cursorIndexOfStartDate);
            }
            final Date _tmp_3 = __dateConverter.fromTimestamp(_tmp_2);
            if(_tmp_3 == null) {
              throw new IllegalStateException("Expected non-null java.util.Date, but it was null.");
            } else {
              _tmpStartDate = _tmp_3;
            }
            final Date _tmpEndDate;
            final Long _tmp_4;
            if (_cursor.isNull(_cursorIndexOfEndDate)) {
              _tmp_4 = null;
            } else {
              _tmp_4 = _cursor.getLong(_cursorIndexOfEndDate);
            }
            final Date _tmp_5 = __dateConverter.fromTimestamp(_tmp_4);
            if(_tmp_5 == null) {
              throw new IllegalStateException("Expected non-null java.util.Date, but it was null.");
            } else {
              _tmpEndDate = _tmp_5;
            }
            final int _tmpWarningThreshold;
            _tmpWarningThreshold = _cursor.getInt(_cursorIndexOfWarningThreshold);
            final List<String> _tmpCategories;
            final String _tmp_6;
            if (_cursor.isNull(_cursorIndexOfCategories)) {
              _tmp_6 = null;
            } else {
              _tmp_6 = _cursor.getString(_cursorIndexOfCategories);
            }
            _tmpCategories = __stringListConverter.toStringList(_tmp_6);
            _item = new BudgetEntity(_tmpId,_tmpAmount,_tmpStartDate,_tmpEndDate,_tmpWarningThreshold,_tmpCategories);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
