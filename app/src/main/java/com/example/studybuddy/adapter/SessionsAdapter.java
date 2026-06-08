package com.example.studybuddy.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studybuddy.R;
import com.example.studybuddy.SessionDetailsActivity;
import com.example.studybuddy.model.Session;

import java.util.ArrayList;

public class SessionsAdapter extends RecyclerView.Adapter<SessionsAdapter.SessionViewHolder> {

    private Context context;
    private ArrayList<Session> sessionList;

    public SessionsAdapter(Context context, ArrayList<Session> sessionList) {
        this.context = context;
        this.sessionList = sessionList;
    }

    @NonNull
    @Override
    public SessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_session, parent, false);
        return new SessionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionViewHolder holder, int position) {
        Session session = sessionList.get(position);

        holder.tvCourse.setText(session.getCourseName());
        holder.tvTopic.setText(session.getTopic());
        holder.tvMeta.setText(
                session.getTime() + " • " + session.getLocation() + " • "
                        + session.getParticipants().size() + "/" + session.getMaxParticipants()
        );

        holder.btnOpen.setOnClickListener(v -> {
            Intent intent = new Intent(context, SessionDetailsActivity.class);
            intent.putExtra("session", session);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return sessionList.size();
    }

    public void updateList(ArrayList<Session> newList) {
        sessionList = newList;
        notifyDataSetChanged();
    }

    public static class SessionViewHolder extends RecyclerView.ViewHolder {

        TextView tvCourse, tvTopic, tvMeta;
        Button btnOpen;

        public SessionViewHolder(@NonNull View itemView) {
            super(itemView);

            tvCourse = itemView.findViewById(R.id.tvCourse);
            tvTopic = itemView.findViewById(R.id.tvTopic);
            tvMeta = itemView.findViewById(R.id.tvMeta);
            btnOpen = itemView.findViewById(R.id.btnOpen);
        }
    }
}