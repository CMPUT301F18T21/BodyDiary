package com.erikligai.doctorplzsaveme.backend;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.erikligai.doctorplzsaveme.ElasticsearchProblemController;
import com.erikligai.doctorplzsaveme.Models.Comment;
import com.erikligai.doctorplzsaveme.Models.Patient;
import com.erikligai.doctorplzsaveme.Models.Problem;
import com.erikligai.doctorplzsaveme.Models.Record;
import com.erikligai.doctorplzsaveme.StartAppActivities.MainActivity;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
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
        try {
            isConnected(); // will throw exception if phone is offline
            assert(patientProfile != null);
            String UserID = patientProfile.getID();
            ElasticsearchProblemController.GetPatientTask getPatientTask = new ElasticsearchProblemController.GetPatientTask();
            Patient es_patient = getPatientTask.execute(UserID).get();
            if (es_patient != null)
            {
            /* TODO:
            for each Problem from DB, take Problem.comments and overwrite local Problem comments
            (in case Care Provider has made new comments), then take local patientProfile and push
            it to the DB */
                patientProfile = es_patient;

            }
            ElasticsearchProblemController.SetPatientTask setPatientTask = new ElasticsearchProblemController.SetPatientTask();
            setPatientTask.execute(patientProfile);
            // else try push local patientProfile to DB (since might be new profile)
        } catch (Exception e)
        {
            // do nothing since we are offline
            Log.i("COULD NOT SYNC WITH ES!", "Failure");
        }

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

    // https://stackoverflow.com/questions/9570237/android-check-internet-connection
    // razzak
    public static boolean isConnected() throws InterruptedException, IOException {
        final String command = "ping -c 1 google.com";
        return Runtime.getRuntime().exec(command).waitFor() == 0;
    }

    public static boolean userIDExists(String UserID)
    {
        return false;
        /*
        ElasticsearchProblemController.CheckIfPatientIDExistsTask checkTask =
                new ElasticsearchProblemController.CheckIfPatientIDExistsTask();
        try {
            return (checkTask.execute(UserID).get());
        } catch (Exception e)
        {
            return false;
        }
        */
    }

    // ICareProviderBackend CODE ------------

    // CP ONLY PULLS FROM DB WHEN LOGGING IN, AND ADDING/DELETING PATIENTS!

    // TODO: ASSERTIONS

    private ArrayList<Patient> patients = null;

    // patient list adapts to this
    public ArrayList<Patient> getPatients() {
        return patients;
    }

    // adds comment to the patient's problem and updates that patient profile to DB
    public void addComment(int patientIndex, int problemIndex, String comment)
    {
        patients.get(patientIndex).getProblemList().get(problemIndex).addComment(new Comment(comment));
        UpdatePatient(patients.get(patientIndex));
    }

    // add patient to CP, PatientID would be aquired from QR code
    public void AddPatient(String PatientID)
    {
        // TODO: update DB patientIDs, and add that patientID's Patient to patients
        // requires error checking (like ID already exists in patients, or doesn't exist on DB)
    }

    // remove patient from CP (not required!) PatientID would be aquired from the Patient class
    public void RemovePatient(String PatientID)
    {
        // TODO: update DB patientIDs, and remove that patientID's Patient from patients
    }

    private void UpdatePatient(Patient patient)
    {
        // TODO: push that Patient to DB
    }

    public void GetPatients()
    {
        // TODO: populate patients from DB Patients
    }
}
