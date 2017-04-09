package com.andre.solid.solidchat.adapters;

import android.databinding.BindingAdapter;
import android.support.annotation.BinderThread;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.andre.solid.solidchat.MyApplication;
import com.andre.solid.solidchat.stuff.DataSetInterface;
import com.bumptech.glide.Glide;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Created by lantain on 08.04.17.
 */

public class Bindings {


    @BindingAdapter({"data"})
    public static void setDataToRecyclerView(RecyclerView recyclerView, List data) {
        if (recyclerView.getAdapter() != null && recyclerView.getAdapter() instanceof DataSetInterface && data != null) {
            ((DataSetInterface) recyclerView.getAdapter()).setData(data);
        }
    }

    @BindingAdapter({"adapter"})
    public static void setAdapterToRv(RecyclerView rv, RecyclerView.Adapter adapter) {
        rv.setAdapter(adapter);
    }

    @BindingAdapter({"data", "adapter"})
    public static void setDataAndAdapterToRv(RecyclerView rv, List data, RecyclerView.Adapter adapter) {
        setAdapterToRv(rv, adapter);
        setDataToRecyclerView(rv, data);
    }

    @BindingAdapter({"data", "adapter"})
    public static void setDataAndAdapterToRv(RecyclerView rv, List data, Class<RecyclerView.Adapter> clazz) {
        Constructor<?> ctor = null;
        try {
            ctor = clazz.getConstructor();
            RecyclerView.Adapter adapter = (RecyclerView.Adapter) ctor.newInstance();
            setAdapterToRv(rv, adapter);
            setDataToRecyclerView(rv, data);

        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @BindingAdapter({"divider"})
    public static void attachDividerToRv(RecyclerView rv, String dividerType) {
        if (dividerType == null)
            return;
        switch (dividerType) {
            case "horizontal": {
                rv.addItemDecoration(new DividerItemDecoration(rv.getContext(), DividerItemDecoration.HORIZONTAL));
                break;
            }
            case "vertical": {
                rv.addItemDecoration(new DividerItemDecoration(rv.getContext(), DividerItemDecoration.VERTICAL));
                break;
            }
        }
    }

    @BindingAdapter({"android:visibility"})
    public static void setVisibility(View view, boolean isVisible) {
        view.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }


    @BindingAdapter({"layout_marginLeft"})
    public static void setMarginLeft(View layout, float margin) {
        if (layout.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) layout.getLayoutParams();
            params.leftMargin = (int) margin;
            layout.setLayoutParams(params);
        }
    }

    @BindingAdapter({"layout_marginRight"})
    public static void setMarginRight(View layout, float margin) {
        if (layout.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) layout.getLayoutParams();
            params.rightMargin = (int) margin;
            layout.setLayoutParams(params);
        }
    }

    @BindingAdapter(value = {"layout_marginTop", "android:layout_marginTop"}, requireAll = false)
    public static void setMarginTop(View layout, float margin, float andrMargin) {
        if (margin == 0)
            margin = andrMargin;

        if (layout.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) layout.getLayoutParams();
            params.topMargin = (int) margin;
            layout.setLayoutParams(params);
        }
    }

    @BindingAdapter({"layout_marginBottom"})
    public static void setMarginBottom(View layout, float margin) {
        if (layout.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) layout.getLayoutParams();
            params.bottomMargin = (int) margin;
            layout.setLayoutParams(params);
        }
    }


    @BindingAdapter({"src"})
    public static void bindUserImageToIv(ImageView view, String url) {
        Glide.with(MyApplication.get()).load(url).into(view);
    }

    @BindingAdapter({"src", "text"})
    public static void bindUserImageToIv(ImageView view, String url, String name) {
        bindImageToIv(view, url, name, 2);
    }


    public static void bindImageToIv(ImageView view, String url, String text, int borderW) {
        ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
        int color = text == null ? generator.getRandomColor() : generator.getColor(text);

        if (text == null)
            text = "U N";

        String stringToDisplay = text.substring(0,1).toUpperCase();

        TextDrawable.IBuilder builder = TextDrawable.builder()
                .beginConfig()
                .withBorder(borderW)
                .width(120)
                .height(120)
                .endConfig()
                .round();

        TextDrawable drawable = builder.build(stringToDisplay, color);

//        view.setImageDrawable(drawable);
        Glide.with(view.getContext().getApplicationContext()).load(url).dontAnimate().placeholder(drawable).into(view);
    }
}
