package com.bitespeed.identity.controller;

import com.bitespeed.identity.dto.IdentifyRequest;
import com.bitespeed.identity.dto.IdentifyResponse;
import com.bitespeed.identity.service.ContactService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/identify")
public class IdentifyController {

    private final ContactService contactService;

    public IdentifyController(ContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping
    public IdentifyResponse identify(@RequestBody IdentifyRequest request) {
        return contactService.identify(request);
    }
}