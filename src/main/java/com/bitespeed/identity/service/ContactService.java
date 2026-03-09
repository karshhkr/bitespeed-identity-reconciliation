package com.bitespeed.identity.service;

import com.bitespeed.identity.dto.ContactData;
import com.bitespeed.identity.dto.IdentifyRequest;
import com.bitespeed.identity.dto.IdentifyResponse;
import com.bitespeed.identity.entity.Contact;
import com.bitespeed.identity.repository.ContactRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ContactService {

    private final ContactRepository contactRepository;

    public ContactService(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    public IdentifyResponse identify(IdentifyRequest request) {

        String email = request.getEmail();
        String phone = request.getPhoneNumber();

        List<Contact> contacts =
                contactRepository.findByEmailOrPhoneNumber(email, phone);

        Contact primary;

        // CASE 1 → No contact exists
        if (contacts.isEmpty()) {

            Contact newContact = new Contact();
            newContact.setEmail(email);
            newContact.setPhoneNumber(phone);
            newContact.setLinkPrecedence("primary");

            primary = contactRepository.save(newContact);

        } else {

            // Find oldest primary
            primary = contacts.stream()
                    .filter(c -> "primary".equals(c.getLinkPrecedence()))
                    .min(Comparator.comparing(Contact::getCreatedAt))
                    .orElse(contacts.get(0));

            // If matched contact was secondary → get its primary
            if ("secondary".equals(primary.getLinkPrecedence())) {

                primary = contactRepository
                        .findById(primary.getLinkedId())
                        .orElse(primary);
            }

            boolean newInfo =
                    contacts.stream().noneMatch(c ->
                            Objects.equals(c.getEmail(), email)
                                    && Objects.equals(c.getPhoneNumber(), phone));

            // Create secondary contact if new info
            if (newInfo) {

                Contact secondary = new Contact();
                secondary.setEmail(email);
                secondary.setPhoneNumber(phone);
                secondary.setLinkedId(primary.getId());
                secondary.setLinkPrecedence("secondary");

                contactRepository.save(secondary);
            }
        }

        // Fetch related contacts
        List<Contact> related = new ArrayList<>();

        related.add(primary);
        related.addAll(contactRepository.findByLinkedId(primary.getId()));

        // Collect response data
        Set<String> emails = new LinkedHashSet<>();
        Set<String> phones = new LinkedHashSet<>();
        List<Long> secondaryIds = new ArrayList<>();

        // Add primary first
        if (primary.getEmail() != null)
            emails.add(primary.getEmail());

        if (primary.getPhoneNumber() != null)
            phones.add(primary.getPhoneNumber());

        for (Contact c : related) {

            if ("secondary".equals(c.getLinkPrecedence())) {

                if (c.getEmail() != null)
                    emails.add(c.getEmail());

                if (c.getPhoneNumber() != null)
                    phones.add(c.getPhoneNumber());

                secondaryIds.add(c.getId());
            }
        }

        // Build response
        ContactData data = new ContactData();

        data.setPrimaryContactId(primary.getId());
        data.setEmails(new ArrayList<>(emails));
        data.setPhoneNumbers(new ArrayList<>(phones));
        data.setSecondaryContactIds(secondaryIds);

        IdentifyResponse response = new IdentifyResponse();
        response.setContact(data);

        return response;
    }
}