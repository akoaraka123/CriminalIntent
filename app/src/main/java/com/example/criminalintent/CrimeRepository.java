package com.example.criminalintent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class CrimeRepository {
    private static CrimeRepository sInstance;

    private final List<Crime> crimes = new ArrayList<>();

    private CrimeRepository() { 
        for (int i = 0; i < 20; i++) {
            Crime crime = new Crime(UUID.randomUUID(),
                    "Crime #" + i, new Date(), i % 3 == 0);
            crime.setRequiresPolice(i % 5 == 0);
            crimes.add(crime);
        }
    }

    public static CrimeRepository get() {
        if (sInstance == null) {
            sInstance = new CrimeRepository();
        }
        return sInstance;
    }

    public List<Crime> getCrimes() {
        return crimes;
    }

    public Crime getCrime(UUID id) {
        if (id == null) return null;
        for (Crime crime : crimes) {
            if (id.equals(crime.getId())) {
                return crime;
            }
        }
        return null;
    }

    public void addCrime(Crime crime) {
        if (crime == null) return;
        crimes.add(0, crime);
    }
}
