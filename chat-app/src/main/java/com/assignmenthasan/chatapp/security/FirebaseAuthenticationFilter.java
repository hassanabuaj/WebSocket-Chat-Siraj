package com.assignmenthasan.chatapp.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * מסנן אימות המבוסס על Firebase ID Token.
 * קורא Authorization: (איש שני) Bearer <token>, מאמת מול Firebase,
 * ובמקרה הצלחה מאכלס Authentication ב-SecurityContext.
 * במקרה כשל אימות – מחזיר 401 ועוצר את השרשרת.
 */
public class FirebaseAuthenticationFilter extends OncePerRequestFilter {
    /**
     * רץ פעם אחת לכל בקשה HTTP.
     * @implNote אם אין Authorization או שלא מתחיל ב-"Bearer ", המסנן פשוט ממשיך בשרשרת בלי לאמת.
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        // בדיקת פורמט Bearer בסיסי
        if (header != null && header.startsWith("Bearer ")) {
            String idToken = header.substring(7);
            try {

                // אימות ה-ID Token מול Firebase
                FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
                AbstractAuthenticationToken auth = new AbstractAuthenticationToken(
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))) {
                    @Override public Object getCredentials() { return idToken; }
                    @Override public Object getPrincipal() { return decodedToken.getUid(); }
                };
                auth.setAuthenticated(true);// מסומן כמוכח
                // ניתן גם: auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // הצבת האותנטיקציה בקונטקסט עבור המשך השרשרת
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception e) {

                // טוקן שגוי/פג תוקף/לא ניתן לאימות – החזר 401 ואל תמשיך בשרשרת
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Firebase token");
                return;
            }
        }
        // המשך עיבוד – או כאנונימי (אם אין טוקן) או כמאומת (אם הצליח)
        filterChain.doFilter(request, response);
    }
}
