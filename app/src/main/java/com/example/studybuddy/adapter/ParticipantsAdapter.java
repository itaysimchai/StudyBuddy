package com.example.studybuddy.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studybuddy.R;
import com.example.studybuddy.model.Student;

import java.util.ArrayList;

public class ParticipantsAdapter extends RecyclerView.Adapter<ParticipantsAdapter.ParticipantViewHolder> {

    private ArrayList<Student> participants;

    public ParticipantsAdapter(ArrayList<Student> participants) {
        this.participants = participants;
    }

    @NonNull
    @Override
    public ParticipantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_participant, parent, false);
        return new ParticipantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParticipantViewHolder holder, int position) {
        Student student = participants.get(position);
        holder.tvName.setText(student.getName());
        holder.tvMajor.setText(student.getMajor());
    }

    @Override
    public int getItemCount() {
        return participants.size();
    }

    public static class ParticipantViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvMajor;

        public ParticipantViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvMajor = itemView.findViewById(R.id.tvMajor);
        }
    }
}