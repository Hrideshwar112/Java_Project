package com.example.shorturl.controller;

import com.example.shorturl.service.UrlService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
public class ShortenerController {

    private final UrlService urlService;

    @Value("${app.baseUrl:http://localhost:8080}")
    private String baseUrl;

    public ShortenerController(UrlService urlService) {
        this.urlService = urlService;
    }

    
    @GetMapping("/")
    public String home() {
        System.out.println(" Home() called");
        return "forward:/index.html";
    }

    @GetMapping("/{code:[a-zA-Z0-9]+}")
    public Object redirect(@PathVariable String code, HttpServletResponse response) throws IOException {
        System.out.println(" Redirect() called for code=" + code);
        String target = urlService.resolve(code);
        if (target == null) {
            System.out.println(" Code not found, redirecting to /");
            response.sendRedirect("/");
            return null;
        }
        return new RedirectView(target);
    }

    //  API endpoint to shorten a URL
    @PostMapping("/api/shorten")
    @ResponseBody
    public ResponseEntity<Map<String, String>> shorten(@RequestBody Map<String, String> payload) {
        String url = payload.get("url");
        String alias = payload.get("alias");

        if (!StringUtils.hasText(url)) {
            return ResponseEntity.badRequest().body(Map.of("error", "url is required"));
        }

        if (StringUtils.hasText(alias)) {
            boolean ok = urlService.createWithAlias(alias, url);
            if (!ok) {
                return ResponseEntity.status(409).body(Map.of("error", "alias already taken"));
            }
            String shortUrl = baseUrl + "/" + alias;
            return ResponseEntity.ok(Map.of("shortUrl", shortUrl, "code", alias));
        }

        String code = urlService.shorten(url);
        String shortUrl = baseUrl + "/" + code;

        Map<String, String> resp = new HashMap<>();
        resp.put("shortUrl", shortUrl);
        resp.put("code", code);
        return ResponseEntity.ok(resp);
    }
}
