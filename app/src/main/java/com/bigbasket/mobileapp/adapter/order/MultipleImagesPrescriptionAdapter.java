package com.bigbasket.mobileapp.adapter.order;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.util.TouchImageView;
import com.bigbasket.mobileapp.util.UIUtil;

import java.util.ArrayList;


public class MultipleImagesPrescriptionAdapter<T> extends BaseAdapter {

    private T ctx;
    private ArrayList<Object> imageUploadModelArrayList;
    private LayoutInflater inflater;

    public MultipleImagesPrescriptionAdapter(T ctx,
                                             ArrayList<Object> imageUploadModelArrayList) {
        this.ctx = ctx;
        this.imageUploadModelArrayList = imageUploadModelArrayList;
        this.inflater = (LayoutInflater) ((ActivityAware) ctx).getCurrentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return imageUploadModelArrayList.size();
    }

    public Object getItem(int position) {
        return imageUploadModelArrayList.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        Object imageUrl = getItem(position);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.multiple_image_list, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (imageUrl instanceof String) {
            TouchImageView imgNewPrescriptionImage = viewHolder.getImgNewPrescriptionImage();
            if ((String.valueOf(imageUrl)) != null) {
                UIUtil.displayAsyncImage(imgNewPrescriptionImage, String.valueOf(imageUrl));
            } else {
                imgNewPrescriptionImage.setImageResource(R.drawable.image_404);
            }
        } else {
            Bitmap bitmapImage = (Bitmap) imageUrl;
            ImageView imgNewPrescriptionImage = viewHolder.getImgNewPrescriptionImage();
            imgNewPrescriptionImage.setImageBitmap(bitmapImage);
        }


        return convertView;
    }

    private class ViewHolder {
        public View base;
        private TouchImageView imgNewPrescriptionImage;

        public ViewHolder(View base) {
            this.base = base;
        }

        public TouchImageView getImgNewPrescriptionImage() {
            if (imgNewPrescriptionImage == null)
                imgNewPrescriptionImage = (TouchImageView) base.findViewById(R.id.imgNewPrescriptionImage);
            return imgNewPrescriptionImage;
        }

    }

}
