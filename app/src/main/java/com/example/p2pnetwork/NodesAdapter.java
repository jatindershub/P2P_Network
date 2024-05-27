package com.example.p2pnetwork;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NodesAdapter extends RecyclerView.Adapter<NodesAdapter.NodeViewHolder> {

    private List<NodeInfo> nodeList;

    public NodesAdapter(List<NodeInfo> nodeList) {
        this.nodeList = nodeList;
    }

    @NonNull
    @Override
    public NodeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.node_item, parent, false);
        return new NodeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NodeViewHolder holder, int position) {
        NodeInfo node = nodeList.get(position);
        holder.nodeId.setText("Node ID: " + node.getNodeId().toString());
        holder.nodeIp.setText("IP: " + node.getIp().getHostAddress());
        holder.nodePort.setText("Port: " + node.getPort());
    }

    @Override
    public int getItemCount() {
        return nodeList.size();
    }

    public void updateNodes(List<NodeInfo> newNodeList) {
        nodeList = newNodeList;
        notifyDataSetChanged();
    }

    static class NodeViewHolder extends RecyclerView.ViewHolder {
        TextView nodeId, nodeIp, nodePort;

        NodeViewHolder(View itemView) {
            super(itemView);
            nodeId = itemView.findViewById(R.id.nodeId);
            nodeIp = itemView.findViewById(R.id.nodeIp);
            nodePort = itemView.findViewById(R.id.nodePort);
        }
    }
}
