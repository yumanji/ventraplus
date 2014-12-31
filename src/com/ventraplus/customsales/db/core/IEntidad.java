package com.ventraplus.customsales.db.core;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public abstract class IEntidad{	
	DBHelper dbHelper;
	public IEntidad(Context context) {
		//super(context);
		dbHelper=DBHelper.getInstance(context);
	}

	public abstract String getTabla();
	
	public abstract String[] getColumnas();
	
	public abstract String[] getPrimaryKeys();

    private String[] primaryKeysValues=new String[getPrimaryKeys().length];
    public void setPrimaryKeysValues(int index, String value){
    	primaryKeysValues[index]=value;
    }
    
    public String[] getPrimaryKeysValues(){
    	return primaryKeysValues;
    }
    
    private boolean cargado = false;
    public boolean isCargado(){
        return cargado;
    }
    
    public void setCargado(boolean value){
    	cargado = value;
    }

    public int isPrimaryKey(String key)
    {
    	String[] primaryKeys=getPrimaryKeys();
        for (int i = 0; i < primaryKeys.length; i++)
        {
            if (key.equals(primaryKeys[i]))
                return i;
        }

        return -1;
    }

    public void setPrimaryKey(String key, String value)
    {
        if (!isCargado())
        {
            int indexKey = isPrimaryKey(key);
            if (indexKey != -1)
            {
            	setPrimaryKeysValues(indexKey, value);
            }
        }
    }

    protected String generarSelect()
    {
    	String[] columns=getColumnas();
        String select = "SELECT ";
        for (int i = 0; i < columns.length; i++)
        {
            select += columns[i];
            if (i < columns.length - 1)
            {
                select += ", ";
            }
            else
            {
                select += " ";
            }
        }
        return select;
    }

    protected String generarWhere()
    {
    	String[] primaryKeys=getPrimaryKeys();
    	String[] primaryKeysValues=getPrimaryKeysValues();
        String where = " ";
        for (int i = 0; i < primaryKeys.length; i++)
        {
            where += primaryKeys[i] + "='";

            String value = primaryKeysValues[i];
            if(value == null)
            {
                return "";
            }

            where += value + "'";

            if (i < primaryKeys.length - 1)
            {
                where += " AND ";
            }
            else
            {
                where += " ";
            }
        }
        return where;
    }

    public boolean cargar(String where)
    {
    	boolean result = false;
        String sql = generarSelect();

        if (where==null || where.equals(""))
            sql += "FROM " + getTabla();
        else
            sql += "FROM " + getTabla() + " WHERE " + where;

        Cursor cursor=dbHelper.getDataBase().rawQuery(sql, null);
        if (cursor.moveToFirst())
        {
            cargarCursor(cursor);
            result = true;
        }
        if (cursor != null && !cursor.isClosed())
		{
			cursor.close();
		}

        return result;
    }

    public boolean cargar()
    {
        boolean result = false;
        String sql = generarSelect();

        String where = generarWhere();
        if (where==null || where.equals(""))
        {
            return false;
        }

        sql += "FROM " + getTabla() +" WHERE "+ where;

        Cursor cursor=dbHelper.getDataBase().rawQuery(sql, null);
        if (cursor.moveToFirst())
        {
            cargarCursor(cursor);
            result = true;
        }
        if (cursor != null && !cursor.isClosed())
		{
			cursor.close();
		}
        return result;
    }

    public ArrayList<IEntidad> buscar(String where)
    {
    	ArrayList<IEntidad> result = new ArrayList<IEntidad>();
        String sql = generarSelect();

        if (where==null || where.equals(""))
            sql += "FROM " + getTabla();
        else
            sql += "FROM " + getTabla() + " WHERE " + where;

        Cursor cursor=dbHelper.getDataBase().rawQuery(sql, null);
        if (cursor.moveToFirst())
		{	
			do{
	            Object aux = cargarCursorList(cursor);
	            if (aux != null)
	                result.add((IEntidad)aux);
            }while(cursor.moveToNext());
		}
        if (cursor != null && !cursor.isClosed())
		{
			cursor.close();
		}

        return result;
    }

    public ArrayList<IEntidad> buscarSQL(String sql)
    {
        ArrayList<IEntidad> result = new ArrayList<IEntidad>();
        Cursor cursor=dbHelper.getDataBase().rawQuery(sql, null);
        if (cursor.moveToFirst())
		{	
			do{
	            Object aux = cargarCursorList(cursor);
	            if (aux != null)
	                result.add((IEntidad)aux);
            }while(cursor.moveToNext());
		}
        if (cursor != null && !cursor.isClosed())
		{
			cursor.close();
		}

        return result;
    }

    public abstract ContentValues getValues();

    public int insertar()
    {
    	ContentValues contentValues = getValues();
        int result = (int) dbHelper.getDataBase().insert(getTabla(), null, contentValues);

        if (result > 0)
        {
            cargado = true;
        }
        return result;
    }

    public int consolidar()
    {
        if (!isCargado())
            return -1;
        String where = generarWhere();
        if (where==null || where.equals(""))
            return -1;

        ContentValues contentValues = getValues();
        int result = (int) dbHelper.getDataBase().update(getTabla(), contentValues, where, null);

        return result;
    }

    public int eliminar(String where)
    {
    	if (where==null || where.equals(""))
        {
            return -1;
        }
    	
    	int result = dbHelper.getDataBase().delete(getTabla(), where, null);

        return result;
    }

    public int eliminar()
    {
        String where = generarWhere();
        if (where==null || where.equals(""))
        {
            return -1;
        }

        int result = dbHelper.getDataBase().delete(getTabla(), where, null);

        return result;
    }

    public int eliminarTabla()
    {
    	int result = dbHelper.getDataBase().delete(getTabla(), null, null);

        return result;
    }

    public abstract void cargarCursor(Cursor cursor);

    public abstract Object cargarCursorList(Cursor cursor);

    protected boolean exist(String column, String[] columnsNames)
    {
        for (int i = 0; i < columnsNames.length; i++)
            if (column.trim().toUpperCase().equals(columnsNames[i].trim().toUpperCase()))
                return true;
        return false;
    }
}