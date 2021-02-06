package com.sb.play.adaptor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sb.play.bingo.R;
import com.sb.play.bingo.models.Stat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class StatAdaptor extends RecyclerView.Adapter<StatAdaptor.ViewHolder> {

    private List<Stat> stats;

    public StatAdaptor(Context context, List<Stat> stats) {
        this.stats = stats;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView roomId, winner, players, time;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            roomId = itemView.findViewById(R.id.roomTextView);
            winner = itemView.findViewById(R.id.winnerTextView);
            players = itemView.findViewById(R.id.playersTextView);
            time = itemView.findViewById(R.id.timeTextView);
        }
    }

    @NonNull
    @Override
    public StatAdaptor.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_view, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StatAdaptor.ViewHolder viewHolder, int i) {
        viewHolder.itemView.setTag(stats.get(i));
        viewHolder.roomId.setText(stats.get(i).getRoom());
        viewHolder.winner.setText(stats.get(i).getWinner());
        viewHolder.time.setText(getTime(stats.get(i).getTime()));
        viewHolder.players.setText(getAllPlayers(stats.get(i).getPlayers()));
    }

    private String getTime(Date timestamp) {
        return new SimpleDateFormat("hh:mm a\ndd/MM/yy").format(timestamp);
    }

    private String getAllPlayers(List<String> players) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < players.size(); i++) {
            stringBuilder.append(players.get(i));
            if (i != players.size() - 1) {
                stringBuilder.append(",\n");
            }
        }
        return stringBuilder.toString();
    }

    @Override
    public int getItemCount() {
        return stats.size();
    }
}
