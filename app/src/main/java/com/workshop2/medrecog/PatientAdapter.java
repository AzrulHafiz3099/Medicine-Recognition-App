package com.workshop2.medrecog;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.ViewHolder> {

    private Context context;
    private List<Patient> patientList;

    // Constructor for the Adapter
    public PatientAdapter(Context context, List<Patient> patientList) {
        this.context = context;
        this.patientList = patientList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.patient_card, parent, false);
        return new ViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Patient patient = patientList.get(position);

        holder.name.setText(patient.getName());
        holder.patientNum.setText(String.valueOf(position + 1)); // This keeps the sequence updated
        holder.medicalHistory.setText(patient.getMedicalHistory());

        // Set item click listener
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to UpdatePatient activity
                Intent intent = new Intent(context, UpdatePatient.class);
                intent.putExtra("PatientID", patient.getPatientID()); // Pass PatientID to the new activity
                context.startActivity(intent);
            }
        });

        // Set click listener on the delete icon
        holder.deleteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show confirmation dialog before deleting
                showDeleteConfirmationDialog(position);
            }
        });
    }


    @Override
    public int getItemCount() {
        return patientList.size();
    }

    private void showDeleteConfirmationDialog(int position) {
        // Create an AlertDialog to confirm the deletion
        new AlertDialog.Builder(context)
                .setTitle("Delete Patient")
                .setMessage("Are you sure you want to delete this patient?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Proceed with the deletion if "Yes" is clicked
                        deletePatient(position);
                    }
                })
                .setNegativeButton("No", null) // "No" simply dismisses the dialog
                .show();
    }

    private void deletePatient(int position) {
        Patient patientToDelete = patientList.get(position);

        // Send the delete request to the server
        String url = context.getString(R.string.api_patient);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");

                            if ("success".equals(status)) {
                                // If the deletion was successful, remove the patient from the list
                                patientList.remove(position);
                                notifyItemRemoved(position);

                                // Notify that the rest of the list has changed and should be rebinded
                                notifyItemRangeChanged(position, getItemCount());

                                Toast.makeText(context, "Deleted patient: " + patientToDelete.getName(), Toast.LENGTH_SHORT).show();
                            } else {
                                // If there was an error
                                String message = jsonResponse.getString("message");
                                Toast.makeText(context, "Error: " + message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(context, "Error parsing response", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context, "Error deleting patient", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "deletePatient");
                params.put("patientID", patientToDelete.getPatientID()); // Send the PatientID to be deleted
                return params;
            }
        };

        // Add the request to the Volley queue
        Volley.newRequestQueue(context).add(stringRequest);
    }



    // ViewHolder for the patient card
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, medicalHistory, patientNum;
        ImageView deleteIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.patient_name);
            medicalHistory = itemView.findViewById(R.id.patient_medicalhistory);
            patientNum = itemView.findViewById(R.id.patient_num);
            deleteIcon = itemView.findViewById(R.id.delete_icon); // Assuming the ImageView has id 'delete_icon'
        }
    }
}
