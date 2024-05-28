package com.example.p2pnetwork;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// NodesAdapter.java
public class NodesAdapter extends RecyclerView.Adapter<NodesAdapter.NodeViewHolder> {
    private List<NodeInfo> nodes;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(NodeInfo nodeInfo);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public static class NodeViewHolder extends RecyclerView.ViewHolder {
        public TextView nodeIpTextView;
        public TextView nodePortTextView;
        public TextView nodeIdTextView;

        public NodeViewHolder(View itemView) {
            super(itemView);
            nodeIpTextView = itemView.findViewById(R.id.nodeIp);
            nodePortTextView = itemView.findViewById(R.id.nodePort);
            nodeIdTextView = itemView.findViewById(R.id.nodeIdTextView);
        }

        public void bind(NodeInfo nodeInfo, OnItemClickListener listener) {
            nodeIpTextView.setText("IP: " + nodeInfo.getIp().getHostAddress());
            nodePortTextView.setText("Port: " + nodeInfo.getPort());
            nodeIdTextView.setText("Node ID: " + nodeInfo.getNodeId().toString());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(nodeInfo);
                }
            });
        }
    }

    public NodesAdapter(List<NodeInfo> nodes) {
        this.nodes = nodes;
    }

    @Override
    public NodeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.node_item, parent, false);
        return new NodeViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(NodeViewHolder holder, int position) {
        holder.bind(nodes.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return nodes.size();
    }
}


