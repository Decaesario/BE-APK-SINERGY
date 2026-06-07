package com.impal.gabungyuk.core.service;

import com.impal.gabungyuk.profile.entitiy.Profile;
import com.impal.gabungyuk.profile.repository.ProfileRepository;
import org.springframework.stereotype.Service;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

@Service
public class TimezoneService {

    private static final String DEFAULT_TIMEZONE = "Asia/Jakarta";

    private final ProfileRepository profileRepository;

    public TimezoneService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public String getUserTimezoneOrDefault(Integer userId) {
        if (userId != null) {
            Optional<Profile> p = profileRepository.findByIdPengguna(userId);
            if (p.isPresent() && p.get().getTimezone() != null && !p.get().getTimezone().isBlank()) {
                return normalizeIndonesiaTimezone(p.get().getTimezone());
            }
        }

        return normalizeIndonesiaTimezone(System.getenv().getOrDefault("APP_TIMEZONE", DEFAULT_TIMEZONE));
    }

    public LocalDateTime convertToUserZone(LocalDateTime original, String targetZoneId) {
        if (original == null) return null;

        ZoneId source = ZoneId.systemDefault();
        ZoneId target = ZoneId.of(normalizeIndonesiaTimezone(targetZoneId));

        ZonedDateTime z = original.atZone(source).withZoneSameInstant(target);
        return z.toLocalDateTime();
    }

    public String normalizeIndonesiaTimezone(String timezone) {
        if (timezone == null || timezone.isBlank()) {
            return DEFAULT_TIMEZONE;
        }

        String value = timezone.trim();
        String upper = value.toUpperCase();

        return switch (upper) {
            case "7", "+7", "GMT+7", "UTC+7", "GMT+07", "UTC+07", "GMT+07:00", "UTC+07:00",
                 "WIB", "ASIA/JAKARTA" -> "Asia/Jakarta";
            case "8", "+8", "GMT+8", "UTC+8", "GMT+08", "UTC+08", "GMT+08:00", "UTC+08:00",
                 "WITA", "ASIA/MAKASSAR" -> "Asia/Makassar";
            case "9", "+9", "GMT+9", "UTC+9", "GMT+09", "UTC+09", "GMT+09:00", "UTC+09:00",
                 "WIT", "ASIA/JAYAPURA" -> "Asia/Jayapura";
            default -> normalizeZoneId(value);
        };
    }

    private String normalizeZoneId(String timezone) {
        try {
            ZoneId zoneId = ZoneId.of(timezone);
            if (zoneId.equals(ZoneId.of("Asia/Jakarta"))
                    || zoneId.equals(ZoneId.of("Asia/Makassar"))
                    || zoneId.equals(ZoneId.of("Asia/Jayapura"))) {
                return zoneId.getId();
            }
        } catch (DateTimeException ignored) {
        }

        return DEFAULT_TIMEZONE;
    }
}
