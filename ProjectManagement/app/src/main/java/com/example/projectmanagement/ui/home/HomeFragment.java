package com.example.projectmanagement.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.projectmanagement.databinding.FragmentHomeBinding;
import com.example.projectmanagement.data.model.Project;
import com.example.projectmanagement.ui.adapter.ProjectAdapter;

import java.util.List;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private ProjectAdapter adapter;

    @Nullable @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo adapter (dùng constructor không tham số)
        adapter = new ProjectAdapter();
        binding.rvProject.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );
        binding.rvProject.setAdapter(adapter);

        // Lấy ViewModel và observe dữ liệu
        HomeViewModel vm = new ViewModelProvider(this).get(HomeViewModel.class);
        vm.getProjects().observe(getViewLifecycleOwner(), this::renderProjects);
    }

    private void renderProjects(List<Project> projects) {
        boolean empty = projects == null || projects.isEmpty();
        binding.emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.rvProject.setVisibility(empty ? View.GONE : View.VISIBLE);
        if (!empty) {
            adapter.setData(projects);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
