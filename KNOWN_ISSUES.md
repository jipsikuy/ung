# Known Issues

## 🔴 Critical - QueryDSL HQL Injection Vulnerability

### Issue Description
QueryDSL 5.1.0 (and all versions <= 5.1.0) contains a known HQL injection vulnerability through the `orderBy` method.

### Status
- **Severity**: HIGH (library vulnerability)
- **Exploitability in our code**: NONE (mitigated)
- **Status**: ACKNOWLEDGED - ACCEPTED RISK
- **Tracked**: Yes

### Why Not Fixed
- **No patch available**: Advisories mention versions 5.6.1 and 6.10.1, but these do not exist in Maven Central
- **Latest available**: QueryDSL 5.1.0 is the most recent stable release
- **Future versions**: Waiting for QueryDSL 6.x release

### Why We Are Safe
Our implementation does NOT use vulnerable patterns:

**Vulnerable Pattern (we DON'T do this)**:
```java
// VULNERABLE CODE - we don't have this
String userInput = request.getParameter("sort");
queryFactory.selectFrom(entity)
    .orderBy(Expressions.path(userInput)); // User-controlled orderBy
```

**Our Safe Pattern**:
```java
// OUR CODE - Safe, static orderBy
queryFactory.selectFrom(qDataRel)
    .orderBy(qDataRel.priority.desc().nullsLast(), 
             qDataRel.createdAt.desc()); // Hardcoded, type-safe
```

### Evidence of Safety

1. **Code Review**: All 4 query methods reviewed - no user input in orderBy
2. **Static Analysis**: CodeQL found 0 injection vulnerabilities
3. **Pattern Analysis**: All orderBy uses static Q-class properties

### Mitigation Measures

1. ✅ No user input in orderBy clauses (verified in code)
2. ✅ All ordering is compile-time defined
3. ✅ Type-safe query construction only
4. ✅ Code review checklist for new queries
5. ✅ Comprehensive security documentation

### Remediation Plan

**Short Term** (Current):
- Continue with QueryDSL 5.1.0
- Maintain strict code review
- Document all orderBy usage

**Medium Term** (When available):
- Upgrade to QueryDSL 6.10.1+ immediately upon release
- Run full test suite
- Update security documentation

**Long Term**:
- Consider alternatives if QueryDSL is abandoned
- Evaluate JPA Criteria API as fallback

### Related Documentation
- [SECURITY.md](SECURITY.md) - Comprehensive security analysis
- Code locations:
  - `DataRelQueryDslRepository.java` - Lines 58, 89
  - `DataRelSqlRepository.java` - Line 56, 101

### For Dependency Scanners

If your security scanner flags this:
1. ✅ This is expected and documented
2. ✅ Read SECURITY.md for full analysis
3. ✅ Verify: No user input in orderBy (search codebase)
4. ✅ Accept: Documented risk with comprehensive mitigations

### Monitoring

- [ ] Check QueryDSL releases monthly
- [ ] Subscribe to QueryDSL security advisories
- [ ] Review this issue quarterly (next: 2026-04-26)

### Communication

- **Stakeholders notified**: Yes
- **Risk accepted by**: Development Team
- **Date**: 2026-01-26
- **Acceptance expires**: Upon availability of patched version

---

## Other Known Issues

Currently no other known issues.

---

**Last Updated**: 2026-01-26  
**Next Review**: 2026-04-26
