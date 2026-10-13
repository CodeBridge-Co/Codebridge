package za.co.eduvos.codebridge.backend;

import za.co.eduvos.codebridge.data.Challenge;
import za.co.eduvos.codebridge.data.Company;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Java Backend Repository for Company Challenges & Briefs (Requirement 2).
 * Handles CRUD operations (Create, Read, Update, Delete) and dynamic persistence.
 */
public class CompanyRepository {

    private static final Map<String, Company> companyStore = new HashMap<>();

    static {
        // Initial registered companies and challenges
        List<Challenge> amazonChallenges = new ArrayList<>();
        amazonChallenges.add(new Challenge(101, "Find Prime Substring", "Easy", "Strings · Math", "58%", false, false, 0));
        amazonChallenges.add(new Challenge(102, "Product Search Autocomplete", "Medium", "Arrays · Hash Table", "45%", false, false, 0));
        amazonChallenges.add(new Challenge(103, "AWS Lambda Simulation", "Hard", "System Design · Concurrency", "32%", false, false, 0));

        Company amazon = new Company(1, "Amazon", "Frontend · Backend · System Design", 12,
                "Solve real-world problems used by Amazon engineers. Build your skills and get noticed!", amazonChallenges);
        companyStore.put("Amazon", amazon);

        List<Challenge> microsoftChallenges = new ArrayList<>();
        microsoftChallenges.add(new Challenge(201, "Azure Blob Stream Processor", "Medium", "Cloud · Streams", "50%", false, false, 0));
        microsoftChallenges.add(new Challenge(202, "AI Prompt Router", "Easy", "AI/ML · String", "65%", false, false, 0));

        Company microsoft = new Company(2, "Microsoft", "Full Stack · Cloud · AI", 10,
                "Solve cloud, AI prompt routing, and scalable backend challenges built by Microsoft teams.", microsoftChallenges);
        companyStore.put("Microsoft", microsoft);

        List<Challenge> deloitteChallenges = new ArrayList<>();
        deloitteChallenges.add(new Challenge(301, "Audit Trail Anomaly Filter", "Hard", "SQL · Algorithms", "28%", false, false, 0));
        deloitteChallenges.add(new Challenge(302, "Tax Ledger Balancing", "Easy", "Math · Arrays", "72%", false, false, 0));

        Company deloitte = new Company(3, "Deloitte", "Data Analysis · Algorithms · SQL", 8,
                "Analyze real enterprise ledgers and audit trail data pipeline challenges.", deloitteChallenges);
        companyStore.put("Deloitte", deloitte);

        List<Challenge> discoveryChallenges = new ArrayList<>();
        discoveryChallenges.add(new Challenge(401, "Vitality Rewards Calculator", "Medium", "Logic · Arrays", "55%", false, false, 0));
        discoveryChallenges.add(new Challenge(402, "Health Claim Rule Engine", "Easy", "Validation · String", "68%", false, false, 0));

        Company discovery = new Company(4, "Discovery", "Tech · Data · AI/ML", 6,
                "Build healthcare claim validation and Vitality reward algorithms.", discoveryChallenges);
        companyStore.put("Discovery", discovery);

        List<Challenge> fnbChallenges = new ArrayList<>();
        fnbChallenges.add(new Challenge(501, "eWallet Transaction Stream", "Medium", "APIs · Banking", "48%", false, false, 0));
        fnbChallenges.add(new Challenge(502, "Mobile App Auth Token Validator", "Easy", "Security · Hash", "70%", false, false, 0));

        Company fnb = new Company(5, "FNB", "Software Engineering · APIs · Mobile", 5,
                "Develop secure eWallet and banking transaction stream APIs.", fnbChallenges);
        companyStore.put("FNB", fnb);

        List<Challenge> capitecChallenges = new ArrayList<>();
        capitecChallenges.add(new Challenge(601, "Real-Time Payment Engine", "Medium", "Java / Spring Boot", "52%", false, false, 0));
        capitecChallenges.add(new Challenge(602, "Account Ledger Reconciliation", "Hard", "Java / Data", "35%", false, false, 0));

        Company capitec = new Company(6, "Capitec", "Backend · Java · Problem Solving", 4,
                "Build resilient real-time payment processing microservices in Java.", capitecChallenges);
        companyStore.put("Capitec", capitec);
    }

    public static synchronized List<Company> getAllCompanies() {
        return new ArrayList<>(companyStore.values());
    }

    public static synchronized Company getCompanyByName(String name) {
        if (name == null) return null;
        for (Company c : companyStore.values()) {
            if (c.getName().equalsIgnoreCase(name)) return c;
        }
        return companyStore.get("Amazon");
    }

    // CRUD: Create / Upload Challenge for a Company
    public static synchronized void createCompanyChallenge(String companyName, Challenge challenge) {
        Company comp = getCompanyByName(companyName);
        if (comp != null) {
            comp.getChallenges().add(challenge);
        }
    }

    // CRUD: Update Challenge
    public static synchronized void updateCompanyChallenge(String companyName, Challenge updated) {
        Company comp = getCompanyByName(companyName);
        if (comp != null) {
            List<Challenge> list = comp.getChallenges();
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getId() == updated.getId()) {
                    list.set(i, updated);
                    break;
                }
            }
        }
    }

    // CRUD: Delete Challenge
    public static synchronized void deleteCompanyChallenge(String companyName, int challengeId) {
        Company comp = getCompanyByName(companyName);
        if (comp != null) {
            comp.getChallenges().removeIf(c -> c.getId() == challengeId);
        }
    }
}
