package com.workshop2.medrecog;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class TermAndCondition extends AppCompatActivity {

    private TextView textTnc;
    private TextView titleAcceptanceOfTerms, wordingAcceptanceOfTerms;
    private TextView titleUserAccount, wordingUserAccount;
    private TextView titlePaymentTerms, wordingPaymentTerms;
    private TextView titleUseOfTheApp, wordingUseOfTheApp;
    private TextView titlePrivacyAndDataCollection, wordingPrivacyAndDataCollection;
    private TextView titleThirdPartyLinks, wordingThirdPartyLinks;
    private TextView titleLimitations, wordingLimitations;
    private TextView titleIndemnification, wordingIndemnification;
    private TextView titleTermination, wordingTermination;
    private TextView titleChangesToTerm, wordingChangesToTerm;
    private TextView date, welcome;
    private ImageView imgBack;
    private TextView titleContactInformation, wordingContactInformation;
    private TextView titleGoverningLaw, wordingGoverningLaw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.term_and_condition);

        // Initialize views
        date = findViewById(R.id.date);
        welcome = findViewById(R.id.welcome);
        titleAcceptanceOfTerms = findViewById(R.id.title_acceptance_of_terms);
        wordingAcceptanceOfTerms = findViewById(R.id.wording_acceptance_of_terms);
        titleUserAccount = findViewById(R.id.title_user_account);
        wordingUserAccount = findViewById(R.id.wording_user_account);
        titlePaymentTerms = findViewById(R.id.title_payment_terms);
        wordingPaymentTerms = findViewById(R.id.wording_payment_terms);
        titleUseOfTheApp = findViewById(R.id.title_use_of_the_app);
        wordingUseOfTheApp = findViewById(R.id.wording_use_of_the_app);
        titlePrivacyAndDataCollection = findViewById(R.id.title_privacy_and_data_collection);
        wordingPrivacyAndDataCollection = findViewById(R.id.wording_privacy_and_data_collection);
        titleThirdPartyLinks = findViewById(R.id.title_third_party_links);
        wordingThirdPartyLinks = findViewById(R.id.wording_third_party_links);
        titleLimitations = findViewById(R.id.title_limitations);
        wordingLimitations = findViewById(R.id.wording_limitations);
        titleIndemnification = findViewById(R.id.title_indemnification);
        wordingIndemnification = findViewById(R.id.wording_indemnification);
        titleTermination = findViewById(R.id.title_termination);
        wordingTermination = findViewById(R.id.wording_termination);
        titleChangesToTerm = findViewById(R.id.title_changes_to_term);
        wordingChangesToTerm = findViewById(R.id.wording_changes_to_term);
        imgBack = findViewById(R.id.img_back);
        titleContactInformation = findViewById(R.id.title_contact_information);
        wordingContactInformation = findViewById(R.id.wording_contact_information);
        titleGoverningLaw = findViewById(R.id.title_governing_law);
        wordingGoverningLaw = findViewById(R.id.wording_governing_law);

        // Set section titles
        date.setText("Last updated: 21/1/2025");

        String welcomeTxt = "Welcome to Medicine Recognition App. By using our app, you agree to these Terms and Conditions. " +
                "Please read them carefully before using the services provided by Medicine Recognition App.";
        String aotTxt = "By accessing or using Medicine Recognition App, you agree to comply with and be bound by these Terms and Conditions. " +
                "If you do not agree to these terms, you may not use the App.";
        String uaTxt = "To access certain features of the App, you may be required to create a user account. You agree to provide accurate, current, " +
                "and complete information during the registration process and update such information as necessary. You are responsible for " +
                "maintaining the confidentiality of your account information and for all activities under your account.";
        String ptTxt = "Certain features of the App require payment. By making a payment through the App, you agree to the following terms:\n" +
                "- All payments are processed through a third-party payment processor.\n" +
                "- Prices for services may vary and are subject to change at any time.\n" +
                "- Payment for services is non-refundable unless otherwise stated.\n" +
                "- You agree to pay all fees and charges associated with the service.";
        String uoftaTxt = "Scan Feature: The App may include a feature that allows you to scan text or barcodes. You agree to use this feature only " +
                "for lawful purposes and in compliance with all applicable laws and regulations.\n" +
                "Intellectual Property: All content, features, and functionality of the App, including but not limited to text, graphics, " +
                "logos, icons, images, and software, are owned by [Your App Name] and are protected by intellectual property laws.\n" +
                "Prohibited Activities: You agree not to engage in any activities that may harm, disrupt, or otherwise interfere with the " +
                "functioning of the App or the experience of other users.";
        String padcTxt = "We collect personal data in accordance with our Privacy Policy. By using the App, you consent to the collection and use of " +
                "your information as outlined in the Privacy Policy.\n" +
                "We do not share your personal information with third parties, except as necessary for providing the services you request.";
        String tplasTxt = "The App may contain links to third-party websites or services that are not controlled or operated by us. We are not " +
                "responsible for the content or practices of third-party services. You access these services at your own risk." ;
        String lolTxt = "Medicine Recognition App will not be liable for any damages or losses arising from:\n" +
                "- Your use of the App.\n" +
                "- Any delay, interruption, or malfunction of the App.\n" +
                "- Any errors or inaccuracies in content." ;
        String iTxt = "You agree to indemnify and hold harmless Medicine Recognition App, its affiliates, employees, and agents from any claims, damages, " +
                "liabilities, or expenses arising from your use of the App or violation of these Terms and Conditions.";
        String tTxt = "We reserve the right to suspend or terminate your account if you violate these Terms and Conditions. Upon termination, " +
                "your access to the App will be restricted, and you must cease all use of the App.";
        String cotTxt = "We may update these Terms and Conditions from time to time. Any changes will be posted on this page with an updated " +
                "\"21/1/2025\" date. Continued use of the App after such changes constitutes your acceptance of the updated terms." ;
        String glTxt = "These Terms and Conditions are governed by the laws of Malaysia. Any legal action or proceeding under these terms " +
                "shall be brought exclusively in the courts located in Malaysia." ;
        String ciTxt = "If you have any questions about these Terms and Conditions, please contact us at:\n" +
                "medrecogapp@gmail.com";

        welcome.setText(welcomeTxt);

        titleAcceptanceOfTerms.setText("1. Acceptance of Terms");
        wordingAcceptanceOfTerms.setText(aotTxt);

        titleUserAccount.setText("2. User Account");
        wordingUserAccount.setText(uaTxt);

        titlePaymentTerms.setText("3. Payment Terms");
        wordingPaymentTerms.setText(ptTxt);

        titleUseOfTheApp.setText("4. Use of the App");
        wordingUseOfTheApp.setText(uoftaTxt);

        titlePrivacyAndDataCollection.setText("5. Privacy and Data Collection");
        wordingPrivacyAndDataCollection.setText(padcTxt);

        titleThirdPartyLinks.setText("6. Third-Party Links and Services");
        wordingThirdPartyLinks.setText(tplasTxt);

        titleLimitations.setText("7. Limitations of Liability");
        wordingLimitations.setText(lolTxt);

        titleIndemnification.setText("8. Indemnification");
        wordingIndemnification.setText(iTxt);

        titleTermination.setText("9. Termination");
        wordingTermination.setText(tTxt);

        titleChangesToTerm.setText("10. Changes to Terms");
        wordingChangesToTerm.setText(cotTxt);

        titleGoverningLaw.setText("11. Governing Law");
        wordingGoverningLaw.setText(glTxt);

        titleContactInformation.setText("12. Contact Information");
        wordingContactInformation.setText(ciTxt);



        // Handle back button click
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to the previous screen
                onBackPressed();
            }
        });
    }
}
