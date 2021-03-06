package com.erikligai.doctorplzsaveme.backend;

import com.erikligai.doctorplzsaveme.Models.Patient;
import com.erikligai.doctorplzsaveme.Models.Problem;
import com.erikligai.doctorplzsaveme.Models.Record;

import java.util.ArrayList;

/**
 * This interface outlines the main CP backend features
 * Javadoc is done in Backend
 */
public interface ICareProviderBackend {

    // return cp's assigned patients
    ArrayList<Patient> GetPatients();

    // clears the patients (when CP logs out)
    void ClearPatients();

    // adds comment to the patient's problem and updates that patient profile to DB
    boolean addComment(String PatientID, int problemIndex, String comment);

    // add patient to CP, PatientID would be aquired from QR code or search by username
    void AddPatient(String PatientID);

    // patient list adapts to this (returns assigned patients)
    ArrayList<Patient> getCPPatients();

    // returns the problems of the patient with the ID patientID
    ArrayList<Problem> GetCPPatientProblems(String PatientID);

    // returns the records of the patient with the ID patientID and a given problemIndex
    ArrayList<Record> GetCPPatientRecords(String PatientID, int ProblemIndex);

    // returns the record of the patient with the ID patientID and a given problemIndex and recordIndex
    Record GetCPPatientRecord(String PatientID, int ProblemIndex, int RecordIndex);

    // returns the problem of the patient with the ID patientID and a given problemIndex
    Problem GetCPPatientProblem(String PatientID, int ProblemIndex);

    // populate the assigned patients from db
    void PopulatePatients();
}
