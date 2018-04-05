package com.omorni.custom_views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;

public class Normal_EditText extends EditText 
{

public Normal_EditText(Context context, AttributeSet attrs, int defStyle) 
{
    super(context, attrs, defStyle);
    init();
}

public Normal_EditText(Context context, AttributeSet attrs)
{
    super(context, attrs);
    init();
}

public Normal_EditText(Context context) 
{
    super(context);
    init();
}

private void init() 
{
    if (!isInEditMode()) 
    {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/AxureHandwriting.ttf");
        setTypeface(tf);
    }
}
}