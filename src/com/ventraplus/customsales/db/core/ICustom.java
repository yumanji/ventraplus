package com.ventraplus.customsales.db.core;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public abstract class ICustom {
	DBHelper dbHelper;
	public ICustom(Context context) {
		//super(context);
		dbHelper=DBHelper.getInstance(context);
	}
	
	private final String KEY_ID_CAMPO = "id_campo";
	private final String KEY_VALOR = "valor";
	
	private Hashtable<String, String> valores=new Hashtable<String, String>();

	public abstract String getTabla();
	
	public abstract String getTablaCustom();
	
	public abstract String[] getColumnas();
	
	public abstract String[] getPrimaryKeys();

    private String[] primaryKeysValues=new String[getPrimaryKeys().length];
    public void setPrimaryKeysValues(int index, String value){
    	primaryKeysValues[index]=value;
    }
    
    public void setValores(String key, String valor){
    	valores.put(key, valor);
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
    
    protected ContentValues generarInsert()
    {
    	ContentValues contentValues=new ContentValues();
    	String[] primaryKeys=getPrimaryKeys();
    	String[] primaryKeysValues=getPrimaryKeysValues();
        
    	for (int i = 0; i < primaryKeys.length; i++)
        {
            String value = primaryKeysValues[i];
            String key = primaryKeys[i];
            if(value == null || key == null)
            {
                return null;
            }

            contentValues.put(key, value);
        }
    	if(contentValues.size()==0)
    		return null;
        return contentValues;
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
        
        if(result){
        	String sqlCustom="select "+KEY_ID_CAMPO+", "+KEY_VALOR+" from "+ getTablaCustom()+" WHERE "+generarWhere();
        	Cursor cursorCustom=dbHelper.getDataBase().rawQuery(sqlCustom, null);
            if (cursorCustom.moveToFirst())
            {
            	do{
            		valores.put(cursorCustom.getString(cursorCustom.getColumnIndex(KEY_ID_CAMPO)), cursorCustom.getString(cursorCustom.getColumnIndex(KEY_VALOR)));
            	}while(cursorCustom.moveToNext());
            }
            if (cursorCustom != null && !cursorCustom.isClosed())
    		{
    			cursorCustom.close();
    		}
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
        
        if(result){
        	String sqlCustom="select "+KEY_ID_CAMPO+", "+KEY_VALOR+" from "+ getTablaCustom()+" where "+where;
        	Cursor cursorCustom=dbHelper.getDataBase().rawQuery(sqlCustom, null);
            if (cursorCustom.moveToFirst())
            {
            	do{
            		valores.put(cursorCustom.getString(cursorCustom.getColumnIndex(KEY_ID_CAMPO)), cursorCustom.getString(cursorCustom.getColumnIndex(KEY_VALOR)));
            	}while(cursorCustom.moveToNext());
            }
            if (cursorCustom != null && !cursorCustom.isClosed())
    		{
    			cursorCustom.close();
    		}
        }
        return result;
    }

    public ArrayList<ICustom> buscar(String where)
    {
    	ArrayList<ICustom> result = new ArrayList<ICustom>();
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
	                result.add((ICustom)aux);
            }while(cursor.moveToNext());
		}
        if (cursor != null && !cursor.isClosed())
		{
			cursor.close();
		}
        
        for(int i=0;i<result.size();i++){
        	String sqlCustom="select "+KEY_ID_CAMPO+", "+KEY_VALOR+" from "+ getTablaCustom()+" WHERE "+result.get(i).generarWhere();
        	Cursor cursorCustom=dbHelper.getDataBase().rawQuery(sqlCustom, null);
            if (cursorCustom.moveToFirst())
            {
            	do{
            		result.get(i).setValores(cursorCustom.getString(cursorCustom.getColumnIndex(KEY_ID_CAMPO)), cursorCustom.getString(cursorCustom.getColumnIndex(KEY_VALOR)));
            	}while(cursorCustom.moveToNext());
            }
            if (cursorCustom != null && !cursorCustom.isClosed())
    		{
    			cursorCustom.close();
    		}
        }

        return result;
    }

    public ArrayList<ICustom> buscarSQL(String sql)
    {
        ArrayList<ICustom> result = new ArrayList<ICustom>();
        Cursor cursor=dbHelper.getDataBase().rawQuery(sql, null);
        if (cursor.moveToFirst())
		{	
			do{
	            Object aux = cargarCursorList(cursor);
	            if (aux != null)
	                result.add((ICustom)aux);
            }while(cursor.moveToNext());
		}
        if (cursor != null && !cursor.isClosed())
		{
			cursor.close();
		}
        
        for(int i=0;i<result.size();i++){
        	String sqlCustom="select "+KEY_ID_CAMPO+", "+KEY_VALOR+" from "+ getTablaCustom()+" WHERE "+result.get(i).generarWhere();
        	Cursor cursorCustom=dbHelper.getDataBase().rawQuery(sqlCustom, null);
            if (cursorCustom.moveToFirst())
            {
            	do{
            		result.get(i).setValores(cursorCustom.getString(cursorCustom.getColumnIndex(KEY_ID_CAMPO)), cursorCustom.getString(cursorCustom.getColumnIndex(KEY_VALOR)));
            	}while(cursorCustom.moveToNext());
            }
            if (cursorCustom != null && !cursorCustom.isClosed())
    		{
    			cursorCustom.close();
    		}
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
            
            insertarCustom();
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
        
        eliminarCustom();
        insertarCustom();

        return result;
    }

    public int eliminar(String where)
    {
    	if (where==null || where.equals(""))
        {
            return -1;
        }
    	
    	ArrayList<ICustom> list=buscar(where);
    	
    	int result = dbHelper.getDataBase().delete(getTabla(), where, null);

    	for(int i=0;i<list.size();i++){
    		list.get(i).eliminarCustom();
    	}

        return result;
    }
    
    private void eliminarCustom(){
    	String where = generarWhere();
    	dbHelper.getDataBase().delete(getTablaCustom(), where, null);
    }
    
    private void insertarCustom(){
    	ContentValues contentValuesCustom=generarInsert();
        Enumeration<String> enumeration=valores.keys();
        while(enumeration.hasMoreElements()) {
        	String key=enumeration.nextElement();
        	contentValuesCustom.put(key, valores.get(key));
        }
        
        dbHelper.getDataBase().insert(getTablaCustom(), null, contentValuesCustom);
    }

    public int eliminar()
    {
        String where = generarWhere();
        if (where==null || where.equals(""))
        {
            return -1;
        }

        int result = dbHelper.getDataBase().delete(getTabla(), where, null);
        if(result>0){
        	dbHelper.getDataBase().delete(getTablaCustom(), where, null);
        }

        return result;
    }

    public int eliminarTabla()
    {
    	int result = dbHelper.getDataBase().delete(getTabla(), null, null);
    	dbHelper.getDataBase().delete(getTablaCustom(), null, null);

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