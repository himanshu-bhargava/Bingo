package com.sb.play.adaptor;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sb.play.bingo.R;
import com.sb.play.bingo.models.About;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AboutAdaptor extends RecyclerView.Adapter<AboutAdaptor.ViewHolder> {

    private final List<About> abouts;
    private final Context context;

    public AboutAdaptor(Context context, List<About> abouts) {
        this.abouts = abouts;
        this.context = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView quesTextView;
        public LinearLayout answerLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            quesTextView = itemView.findViewById(R.id.quesTextView);
            answerLayout = itemView.findViewById(R.id.answerLayout);
        }
    }

    private void getAnswerLayout(About.Answer answer, LinearLayout parentLayout, int space) {
        if (answer == null) {
            return;
        }
        LinearLayout verticalLayout = new LinearLayout(context);
        verticalLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout horizontalLayout = new LinearLayout(context);
        horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);
        horizontalLayout.setPadding(space, 0, 0, 0);

        TextView textViewPoint = new TextView(context);
        textViewPoint.setTextColor(Color.WHITE);
        textViewPoint.setText(" • ");
        horizontalLayout.addView(textViewPoint);

        TextView textView = new TextView(context);
        textView.setTextColor(Color.WHITE);
        textView.setText(answer.getMain());
        horizontalLayout.addView(textView);

        verticalLayout.addView(horizontalLayout);
        parentLayout.addView(verticalLayout);
        for (About.Answer sub : answer.getSub()) {
            getAnswerLayout(sub, verticalLayout, space + 24);
        }
    }

    @NonNull
    @Override
    public AboutAdaptor.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_about, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AboutAdaptor.ViewHolder viewHolder, int i) {
        viewHolder.itemView.setTag(i);
        viewHolder.quesTextView.setText(abouts.get(i).getQuestion());
        getAnswerLayout(abouts.get(i).getAnswer(), viewHolder.answerLayout, 0);
    }

    @Override
    public int getItemCount() {
        return abouts.size();
    }
}
