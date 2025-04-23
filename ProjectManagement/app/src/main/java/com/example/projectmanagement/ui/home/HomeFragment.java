package com.example.projectmanagement.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.projectmanagement.databinding.FragmentHomeBinding;
import com.example.projectmanagement.data.model.Project;
import com.example.projectmanagement.ui.adapter.ProjectAdapter;

import java.util.List;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private ProjectAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // Setup RecyclerView
        adapter = new ProjectAdapter(requireContext(), null);
        binding.rvProject.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvProject.setAdapter(adapter);

        // Observe data
        viewModel.getProjects().observe(getViewLifecycleOwner(), this::updateUI);

        return binding.getRoot();
    }

    private void updateUI(List<Project> projects) {
        if (projects == null || projects.isEmpty()) {
            binding.emptyView.setVisibility(View.VISIBLE);
            binding.rvProject.setVisibility(View.GONE);
        } else {
            binding.emptyView.setVisibility(View.GONE);
            binding.rvProject.setVisibility(View.VISIBLE);
            adapter.setData(projects);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
