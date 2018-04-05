package com.omorni.custom_views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

public class Bold_Button_Font extends Button 
{

	public Bold_Button_Font(Context context, AttributeSet attrs, int defStyle) 
	{
    super(context, attrs, defStyle);
    init();
	}

	public Bold_Button_Font(Context context, AttributeSet attrs)
	{
    super(context, attrs);
    init();
	}

	public Bold_Button_Font(Context context) 
	{
    super(context);
    init();
	}

	private void init() 
	{
    if (!isInEditMode()) 
    {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/AxureHandwriting-Bold.ttf");
        setTypeface(tf);
    }
	
	}
}
