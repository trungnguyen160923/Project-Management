package com.example.projectmanagement.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.projectmanagement.data.service.PhaseService;
import com.example.projectmanagement.data.model.Phase;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class PhaseRepository {
    private static final String TAG = "PhaseRepository";
    private static PhaseRepository instance;
    private final Context context;
    private final MutableLiveData<Phase> phaseLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Phase>> phasesLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> messageLiveData = new MutableLiveData<>();

    private PhaseRepository(Context ctx) {
        this.context = ctx.getApplicationContext();
        phasesLiveData.setValue(new ArrayList<>());
    }

    public static PhaseRepository getInstance(Context ctx) {
        if (instance == null) {
            instance = new PhaseRepository(ctx);
        }
        return instance;
    }

    public LiveData<List<Phase>> getProjectPhases(int projectId) {
        final int finalProjectId = projectId;
        PhaseService.getProjectPhases(context, projectId, response -> {
            try {
                if ("success".equals(response.optString("status"))) {
                    List<Phase> phases = PhaseService.parsePhasesList(response);
                    phasesLiveData.setValue(phases);
                    Log.d(TAG, "Loaded " + phases.size() + " phases for project " + finalProjectId);
                } else {
                    String err = response.optString("message", "Lấy phases thất bại");
                    Log.e(TAG, err);
                    phasesLiveData.setValue(null);
                    messageLiveData.setValue(err);
                }
            } catch (JSONException e) {
                Log.e(TAG, "Parsing error", e);
                phasesLiveData.setValue(null);
                messageLiveData.setValue("Lỗi phân tích dữ liệu server");
            }
        }, error -> {
            Log.e(TAG, "Network error", error);
            phasesLiveData.setValue(null);
            messageLiveData.setValue(error.getMessage());
        });

        return phasesLiveData;
    }

    public LiveData<Phase> createPhase(Phase phase) {
        final Phase finalPhase = phase;
        PhaseService.createPhase(context, phase, response -> {
            try {
                if ("success".equals(response.optString("status"))) {
                    Phase createdPhase = PhaseService.parsePhase(response);
                    phaseLiveData.setValue(createdPhase);
                    messageLiveData.setValue("Tạo phase thành công");
                    Log.d(TAG, "Created phase: " + finalPhase.getPhaseName());
                } else {
                    String err = response.optString("message", "Tạo phase thất bại");
                    Log.e(TAG, err);
                    phaseLiveData.setValue(null);
                    messageLiveData.setValue(err);
                }
            } catch (JSONException e) {
                Log.e(TAG, "Parsing error", e);
                phaseLiveData.setValue(null);
                messageLiveData.setValue("Lỗi phân tích dữ liệu server");
            }
        }, error -> {
            Log.e(TAG, "Network error", error);
            phaseLiveData.setValue(null);
            messageLiveData.setValue(error.getMessage());
        });

        return phaseLiveData;
    }

    public LiveData<String> getMessageLiveData() {
        return messageLiveData;
    }
}
