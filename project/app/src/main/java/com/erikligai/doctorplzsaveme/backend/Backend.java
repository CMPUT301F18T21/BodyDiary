package com.erikligai.doctorplzsaveme.backend;

import android.content.Context;

import com.erikligai.doctorplzsaveme.Models.Comment;
import com.erikligai.doctorplzsaveme.Models.Patient;
import com.erikligai.doctorplzsaveme.Models.Problem;
import com.erikligai.doctorplzsaveme.Models.Record;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class Backend implements IPatientBackend, ICareProviderBackend {

    // SINGLETON CODE -----------------------
    private static Backend instance = new Backend();
    public static Backend getInstance() {
        return instance;
    }
    private Backend() {}

    // IPatientBackend CODE -----------------

    // TODO: move this filename to an xml
    private static final String FILENAME = "patient_profile.sav";

    private Context mContext = null; // context for reading to/from file
    // current patient profile
    private Patient patientProfile = null;

    // THIS MUST BE CALLED ON APP STARTUP!
    public void setContext(Context context)
    {
        this.mContext = context;
    }

    // THIS MUST BE CALLED WHENEVER A CHANGE TO PROFILE AND ITS MEMBERS IS MADE
    public void UpdatePatient() {
        // sync with DB
        new Thread(new Runnable() {
            @Override
            public void run() {
                serializePatientProfile();
                syncPatientES();
            }
        }).start();
    }

    public void setPatientProfile(Patient patientProfile) {
        this.patientProfile = patientProfile;
        UpdatePatient();
    }

    // ex. usage
    // if (Backend.getInstance().getPatientProfile() == null) {...}
    public Patient getPatientProfile() {
        assert(patientProfile != null);
        return patientProfile;
    }

    public ArrayList<Problem> getPatientProblems()
    {
        assert(patientProfile != null);
        return patientProfile.getProblemList();
    }

    public ArrayList<Record> getPatientRecords(int problemIndex)
    {
        assert(patientProfile != null);
        return patientProfile.getProblemList().get(problemIndex).getRecords();
    }

    public void addPatientProblem(Problem problem) {
        assert(patientProfile != null);
        patientProfile.addProblem(problem);
        UpdatePatient();
    }

    public void deletePatientProblem(int problemIndex) {
        assert(patientProfile != null);
        patientProfile.deleteProblem(problemIndex);
        UpdatePatient();
    }

    public void addPatientRecord(int problemIndex, Record record) {
        assert(patientProfile != null);
        patientProfile.getProblemList().get(problemIndex).addRecord(record);
        UpdatePatient();
    }

    public void deletePatientRecord(int problemIndex, int recordIndex) {
        assert(patientProfile != null);
        patientProfile.getProblemList().get(problemIndex).getRecords().remove(recordIndex);
        UpdatePatient();
    }

    private void serializePatientProfile()
    {
        try {
            assert(mContext != null);
            assert(patientProfile != null);
            FileOutputStream fos = mContext.getApplicationContext().openFileOutput(FILENAME, 0);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            BufferedWriter writer = new BufferedWriter(osw);
            Gson gson = new Gson();
            gson.toJson(patientProfile, writer);
            writer.flush();
            writer.close();
            fos.close();
            osw.close();
        } catch (IOException e) {
            e.printStackTrace(); // shouldn't happen ever
        }
    }

    private boolean deserializePatientProfile()
    {
        try {
            assert(mContext != null);
            FileInputStream fis = mContext.getApplicationContext().openFileInput(FILENAME);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader reader = new BufferedReader(isr);
            Gson gson = new Gson();
            patientProfile = gson.fromJson(reader, Patient.class);
            return true;
        } catch (IOException e) {
            // we couldn't find it in file, so just make sure its null and return false to
            // notify that we did not find anything on local storage
            patientProfile = null;
            return false;
        }
    }

    private void syncPatientES()
    {
       if (!isConnected()) { return; }
        assert(patientProfile != null);
        String UserID = patientProfile.getID();
        ElasticsearchProblemController.GetPatientTask getPatientTask = new ElasticsearchProblemController.GetPatientTask();
        Patient es_patient = null;
        try {
            es_patient = getPatientTask.execute(UserID).get();
        } catch (Exception e) { }
        if (es_patient != null)
        {
            // overwrite comments of local, in case they have changed
            try {
                for (int i = 0; i < patientProfile.getProblemList().size(); ++i)
                {
                    patientProfile.getProblemList().get(i).setComments(es_patient.getProblemList().get(i).getComments());
                }
            } catch (Exception e ) {}

        }
        ElasticsearchProblemController.SetPatientTask setPatientTask = new ElasticsearchProblemController.SetPatientTask();
        setPatientTask.execute(patientProfile);
    }

    public Patient fetchPatientProfile() {
        // if local storage doesn't have a profile, return null
        if (!deserializePatientProfile()) { return null; }
        else
        {
            // fetch Patient from DB, will overwrite local comments if it can
            new Thread(new Runnable() {
                @Override
                public void run() {
                    syncPatientES();
                }
            }).start();
            assert(patientProfile != null);
            return patientProfile;
        }
    }

    public void setPatientFromES(String UserID)
    {
        patientProfile = null;
        try {
            ElasticsearchProblemController.GetPatientTask getPatientTask = new ElasticsearchProblemController.GetPatientTask();
            patientProfile = getPatientTask.execute(UserID).get();
            serializePatientProfile();
        } catch (Exception e)
        {
            // do nothing, couldn't login
        }
    }

    public void clearLocalData()
    {
        mContext.getApplicationContext().deleteFile(FILENAME);
    }

    // https://stackoverflow.com/questions/9570237/android-check-internet-connection
    // razzak
    public static boolean isConnected(){
        final String command = "ping -c 1 google.com";
        try {
            return Runtime.getRuntime().exec(command).waitFor() == 0;
        } catch (Exception e)
        {
            return false;
        }

    }

    public static boolean userIDExists(String UserID)
    {
        ElasticsearchProblemController.CheckIfPatientIDExistsTask checkTask =
                new ElasticsearchProblemController.CheckIfPatientIDExistsTask();
        try {
            return (checkTask.execute(UserID).get());
        } catch (Exception e)
        {
            return false;
        }
    }

    // ICareProviderBackend CODE ------------

    // CP ONLY PULLS FROM DB WHEN LOGGING IN, AND ADDING/DELETING PATIENTS!

    // TODO: ASSERTIONS

    private ArrayList<Patient> m_patients = null;

    private String CP_ID = null;

    // patient list adapts to this
    public ArrayList<Patient> getM_patients() {
        return m_patients;
    }

    // adds comment to the patient's problem and updates that patient profile to DB
    public void addComment(int patientIndex, int problemIndex, String comment)
    {
        m_patients.get(patientIndex).getProblemList().get(problemIndex).addComment(new Comment(comment));
        UpdatePatient(m_patients.get(patientIndex));
    }

    // add patient to CP, PatientID would be aquired from QR code
    public void AddPatient(String PatientID)
    {
        // TODO: update DB patientIDs, and add that patientID's Patient to m_patients
        // requires error checking (like ID already exists in m_patients, or doesn't exist on DB)
        for (Patient p : m_patients) {
            if (p.getID() == PatientID) { return; }
        }
        ElasticsearchProblemController.AssignPatientToCPTask assignTask = new ElasticsearchProblemController.AssignPatientToCPTask();
        String[] params = new String[]{CP_ID, PatientID};
        assignTask.execute(params);
        ElasticsearchProblemController.GetPatientTask getPatientTask = new ElasticsearchProblemController.GetPatientTask();
        try {
            Patient new_patient = getPatientTask.execute(PatientID).get();
            if (new_patient != null) { m_patients.add(new_patient); }
        } catch (Exception e) {}
    }

    // remove patient from CP (not required!) PatientID would be aquired from the Patient class
    public void RemovePatient(String PatientID)
    {
        // TODO: update DB patientIDs, and remove that patientID's Patient from m_patients
    }

    private void UpdatePatient(Patient patient)
    {
        ElasticsearchProblemController.SetPatientTask setPatientTask = new ElasticsearchProblemController.SetPatientTask();
        setPatientTask.execute(patient);
    }

    public void GetPatients()
    {
        ArrayList<String> PatientIDs = null;
        ElasticsearchProblemController.GetCPPatientsTask getPatientsTask = new ElasticsearchProblemController.GetCPPatientsTask();
        try {
            PatientIDs = getPatientsTask.execute(CP_ID).get();
            for (String patient : PatientIDs)
            {
                ElasticsearchProblemController.GetPatientTask getPatientTask = new ElasticsearchProblemController.GetPatientTask();
                m_patients.add(getPatientTask.execute(patient).get());
            }
        }
        catch (Exception e) {}
    }
}
