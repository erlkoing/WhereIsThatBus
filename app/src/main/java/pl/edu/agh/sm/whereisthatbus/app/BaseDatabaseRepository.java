package pl.edu.agh.sm.whereisthatbus.app;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Vector;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class BaseDatabaseRepository extends SQLiteOpenHelper
{
	protected Context context;
	protected SQLiteDatabase database;
	private final static String DATABASE_NAME = "rozklad.sqlite" ;
	
	
	public BaseDatabaseRepository(Context context)
	{
		super(context, DATABASE_NAME, null, 1);
		this.context = context;
	}

	public void createDatabase()
	{
		File file = new File("/data/data/" + context.getPackageName() + "/databases/" + DATABASE_NAME);

		if (!file.exists())
		{
			this.getReadableDatabase();
			this.copyDatabase();
			this.closeDatabase();
		}
	}

	public void openDatabase(int openDatabaseMode)
	{
		if (database == null)
		{
			if (openDatabaseMode == SQLiteDatabase.OPEN_READONLY)
				database = this.getReadableDatabase();
			else if (openDatabaseMode == SQLiteDatabase.OPEN_READWRITE)
				database = this.getWritableDatabase();
		}
	}

	private void copyDatabase()
	{
		InputStream inputStream;

		try
		{
			inputStream = context.getAssets().open(DATABASE_NAME);
			OutputStream outputStream = new FileOutputStream("/data/data/" + context.getPackageName() + "/databases/" + DATABASE_NAME);

			byte[] buffer = new byte[1024];
			int length;

			while ((length = inputStream.read(buffer)) > 0)
			{
				outputStream.write(buffer, 0, length);
			}

			outputStream.flush();
			outputStream.close();
			inputStream.close();
		} catch (IOException e)
		{
			Log.e("Database", "Copy database - fail");
			e.printStackTrace();
		}
	}

	protected Vector<HashMap<String, String>> executeQuery(String query, String[] params)
	{
		Vector<HashMap<String, String>> results = new Vector<HashMap<String, String>>();
		Cursor cursor = null;
		try
		{
			cursor = database.rawQuery(query, (params != null ? params : null));

			while (cursor.moveToNext())
			{
				HashMap<String, String> result = new HashMap<String, String>();
				int columnCount = cursor.getColumnCount();

				for (int i = 0; i < columnCount; i++)
				{
					result.put(cursor.getColumnNames()[i], cursor.getString(i));
				}
				results.add(result);
			}
		} catch (Exception e)
		{
			Log.e("Database", e.toString());

		} finally
		{
			if (cursor != null)
				cursor.close();
			this.closeDatabase();
		}
		return results;
	}

	public void closeDatabase()
	{
		if (database != null)
		{
			database.close();
			database = null;
		}

		super.close();
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
	}
}
