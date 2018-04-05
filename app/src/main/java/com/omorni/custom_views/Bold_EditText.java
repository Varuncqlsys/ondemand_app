package com.omorni.custom_views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;

public class Bold_EditText extends EditText
{

public Bold_EditText(Context context, AttributeSet attrs, int defStyle)
{
    super(context, attrs, defStyle);
    init();
}

public Bold_EditText(Context context, AttributeSet attrs)
{
    super(context, attrs);
    init();
}

public Bold_EditText(Context context)
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