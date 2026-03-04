# Open Tactics Strategic Plan
**Version:** 1.0  
**Date:** February 9, 2026  
**Objective:** Transform Open Tactics from a functional prototype into a marketable, successful tactical RPG

---

## Vision Statement

**Build a premium Fire Emblem-style tactical RPG for Android that demonstrates quality indie game development is possible without predatory monetization, creating a sustainable product that players genuinely enjoy and recommend to others.**

---

## Core Philosophy

### Design Principles
1. **Player Respect First**: No dark patterns, honest pricing, value for money
2. **Quality Over Quantity**: 20 polished chapters > 100 mediocre ones
3. **Classic Gameplay**: Stay true to Fire Emblem mechanics that work
4. **Mobile-Optimized**: Touch controls and session-friendly gameplay
5. **Community-Driven**: Open source enables player contributions

### Monetization Strategy
**Primary Model:** Premium ($4.99-$9.99) with optional cosmetic DLC  
**Alternative:** Free with single $4.99 unlock after Chapter 3

**Why Premium?**
- Aligns with "player respect first" principle
- Tactical RPG audience prefers premium model
- Simpler development (no complex F2P systems)
- Higher revenue per user than ad-supported
- Better reviews/ratings than F2P competitors

**Revenue Projections (Conservative):**
- Year 1: 10K downloads × $5 × 70% (Google cut) = $35K
- Year 2: 25K downloads × $5 × 70% = $87.5K
- Year 3: 15K downloads × $5 × 70% = $52.5K
- **3-Year Total: ~$175K gross, $122.5K net**

---

## Product Roadmap

### Phase 1: Foundation Polish (Months 1-2)
**Goal:** Fix critical gaps and prepare for visual development

#### Technical Debt Resolution
- [ ] Fix EncryptedSharedPreferences deprecation warnings
- [ ] Implement AI healing (GameState.kt line 383)
- [ ] Add @IgnoredOnParcel annotations (Character.kt)
- [ ] Update serialization annotations (Weapon.kt)
- [ ] Address Gradle 10 compatibility warnings

#### Critical Features
- [ ] **Tutorial Chapter**: 5-turn guided scenario teaching basics
  - Movement, attacking, terrain, leveling
  - Dialogue boxes with instructions
  - No permadeath, just learning
- [ ] **Accessibility improvements**
  - Larger tap targets (min 48dp)
  - Adjustable text size setting
  - Color-blind friendly mode option
- [ ] **Basic polish pass**
  - Consistent button styling
  - Loading screens between chapters
  - Settings menu (volume, text size, etc.)

**Deliverables:**
- ✅ All deprecation warnings resolved
- ✅ Tutorial chapter functional
- ✅ Settings menu working
- ✅ Code passes all quality checks

**Success Criteria:**
- Zero compiler/lint warnings
- Tutorial completion rate >90% in testing
- Settings save/load correctly

---

### Phase 2: Visual Transformation (Months 3-4)
**Goal:** Transform from prototype to visually appealing game

#### Art Assets (Commission or Create)
- [ ] **Character sprites**: 16x16 or 32x32 pixel art for 7 classes
  - Knight, Archer, Mage, Healer, Thief, Pegasus Knight, Enemy variants
  - Idle, walking (4-frame), attacking (3-frame) animations
  - Budget: $50-100 per class = $350-700 total
- [ ] **Terrain tiles**: Consistent pixel art for 6 terrain types
  - 32x32 tiles: Plain, Forest, Mountain, Fort, Village, Water
  - Budget: $200-300 for full tileset
- [ ] **UI elements**: Buttons, frames, cursors, icons
  - Budget: $100-200
- [ ] **Character portraits**: Bust shots for named units (10 characters)
  - Budget: $30-50 per portrait = $300-500

**Total Art Budget: $950-1,700**

#### Animation Implementation
- [ ] Character sprite rendering system
- [ ] Walk animation (4 frames, 200ms each)
- [ ] Attack animation (3 frames with weapon swing)
- [ ] Damage flash effect (red tint on hit)
- [ ] Level-up particle effect (sparkles/stars)
- [ ] Smooth camera movement between units

#### UI Overhaul
- [ ] Replace text labels with sprites
- [ ] Add HP bars above units
- [ ] Animated action buttons
- [ ] Battle forecast shows unit sprites
- [ ] Victory/defeat animated screens

**Deliverables:**
- ✅ All character classes have sprites
- ✅ Terrain renders with tileset
- ✅ Basic animations working
- ✅ UI feels polished

**Success Criteria:**
- Game looks "indie quality" not "prototype"
- 80% of test players prefer visual update
- No performance regressions (60 fps maintained)

---

### Phase 3: Audio & Feel (Month 5)
**Goal:** Add audio feedback and make game feel alive

#### Sound Effects
- [ ] **UI sounds** (5-10 effects)
  - Button clicks, menu navigation, selection
  - Budget: $50-100 for pack
- [ ] **Combat sounds** (10-15 effects)
  - Sword slash, axe thunk, arrow whoosh, magic cast
  - Hit, critical hit, miss
  - Heal sound, level-up chime
  - Budget: $100-200 for pack
- [ ] **Movement sounds** (3-5 effects)
  - Footsteps, horse gallop, wing flap
  - Budget: $50-100
  
**Total SFX Budget: $200-400**

#### Background Music
- [ ] **Menu theme** - Calm, welcoming (1-2 min loop)
- [ ] **Battle theme** - Energetic, tactical (2-3 min loop)
- [ ] **Victory theme** - Triumphant (30s)
- [ ] **Defeat theme** - Somber (30s)

**Options:**
- Commission original: $300-500 per track = $1,200-2,000
- License royalty-free: $50-100 per track = $200-400
- Use open-source: Free but quality varies

**Recommended: Licensed music for MVP ($200-400)**

#### Implementation
- [ ] Audio manager with volume controls
- [ ] Music looping system
- [ ] SFX pooling for performance
- [ ] Settings for music/SFX volume independently

**Deliverables:**
- ✅ All major actions have audio feedback
- ✅ Background music plays during battles
- ✅ Volume controls functional

**Success Criteria:**
- Game feels more engaging with audio
- No audio glitches or performance issues
- Players can customize audio preferences

---

### Phase 4: Content Creation (Months 6-8)
**Goal:** Build 15-20 chapter campaign with basic story

#### Story Development
- [ ] **Setting**: Medieval fantasy kingdom under invasion
- [ ] **Protagonist**: Young commander defending their homeland
- [ ] **Antagonist**: Rival empire led by corrupted ruler
- [ ] **Arc**: Defend → Counterattack → Confrontation → Victory

#### Campaign Structure
**Act 1: Defense (Chapters 1-5)**
- Tutorial + 4 escalating defensive battles
- Introduce main characters and setting
- Difficulty: Easy to Normal

**Act 2: Counterattack (Chapters 6-12)**
- 7 varied missions (escort, survive, defeat commander)
- Recruit additional units
- Introduce enemy variety (promoted classes, bosses)
- Difficulty: Normal

**Act 3: Invasion (Chapters 13-18)**
- 6 challenging maps in enemy territory
- Tougher encounters, strategic objectives
- Story climax building to final battle
- Difficulty: Normal to Hard

**Finale (Chapters 19-20)**
- Two-part final battle against main antagonist
- Epic scale, all units available
- Multiple victory conditions
- Difficulty: Hard

#### Character Writing
- [ ] **10 named units** with personalities and backstories
- [ ] **Dialogue system** for story scenes
- [ ] **Support conversations** between units (unlock at A/S rank)
- [ ] **Victory quotes** after defeating enemies

#### Map Design
- [ ] Design 20 unique map layouts
- [ ] Balance objectives (defeat all, defend, survive, escape)
- [ ] Place strategic terrain features
- [ ] Enemy unit composition and placement
- [ ] Difficulty curve testing

**Deliverables:**
- ✅ 20 playable chapters with story
- ✅ 10 named units with dialogue
- ✅ Balanced difficulty progression

**Success Criteria:**
- Campaign takes 10-15 hours to complete
- Test players complete 80%+ of chapters on first try (Normal)
- Story is coherent and engaging (playtest feedback)

---

### Phase 5: Polish & Testing (Months 9-10)
**Goal:** Refine everything based on feedback

#### Closed Beta Testing
- [ ] Recruit 20-30 testers (Discord, Reddit, friends)
- [ ] Provide TestFlight/Google Play Internal Testing builds
- [ ] Gather structured feedback via surveys
- [ ] Track crashes, bugs, and performance issues

#### Polish Items
- [ ] Fix all critical bugs from testing
- [ ] Balance adjustments based on clear rate data
- [ ] UI/UX improvements from feedback
- [ ] Performance optimization (60 fps on lower-end devices)
- [ ] Battery usage optimization

#### Additional Features (Time Permitting)
- [ ] **Easy/Hard difficulty modes** (scale enemy stats ±20%)
- [ ] **Achievements system** (Google Play Games integration)
- [ ] **Bonus chapters** unlocked after campaign completion
- [ ] **Character viewer** with stats/support history
- [ ] **Replay chapter** feature

**Deliverables:**
- ✅ All critical bugs fixed
- ✅ Game runs smoothly on target devices
- ✅ Balance feels fair and engaging

**Success Criteria:**
- <5 crash reports per 1000 sessions
- 90% of testers rate game 4+ stars
- Campaign completion rate >60%

---

### Phase 6: Marketing & Launch Prep (Months 11-12)
**Goal:** Build awareness and prepare for launch

#### Marketing Assets
- [ ] **Website**: Simple landing page with screenshots, trailer, mailing list
- [ ] **Trailer**: 60-90 second gameplay trailer
  - Show combat, leveling, story snippets
  - Highlight what makes game unique
  - Budget: $300-500 for professional editing (or DIY)
- [ ] **Press Kit**: 
  - Fact sheet, screenshots (15-20 high-quality)
  - Logo assets, developer bio
  - Review copy access
- [ ] **Social Media**:
  - Twitter/X account with gameplay gifs
  - Reddit posts on r/AndroidGaming, r/fireemblem
  - Discord server for community

#### App Store Optimization
- [ ] **Title**: "Open Tactics: Fire Emblem-Style RPG" (or similar)
- [ ] **Description**: SEO-optimized, feature highlights
- [ ] **Screenshots**: 8 compelling images showing gameplay
- [ ] **Video**: 30-second feature graphic or trailer
- [ ] **Keywords**: tactical rpg, fire emblem, strategy, turn-based

#### Launch Strategy
**Soft Launch:** 
- Release to limited regions (e.g., Canada, Australia)
- Gather final feedback and ratings
- Iterate based on reviews

**Full Launch:**
- Worldwide release on Google Play
- Press release to Android gaming sites
- Reddit announcement posts
- Reach out to gaming YouTubers/streamers
- Post on Hacker News (open source angle)

#### Pricing Strategy
**Option A: Premium Launch ($4.99)**
- Immediate revenue
- Filters for interested players
- Higher quality bar (players expect polish)

**Option B: Freemium ($0, $4.99 unlock after Ch3)**
- Lower barrier to entry
- More downloads, reviews
- Requires conversion optimization

**Recommendation: Premium at $4.99** (simpler, aligns with philosophy)

**Deliverables:**
- ✅ Website live with trailer
- ✅ Play Store page optimized
- ✅ Press kit ready
- ✅ Community channels established

**Success Criteria:**
- 500+ wishlists before launch
- 5 gaming sites cover the game
- Launch day: 100+ downloads, 5+ reviews

---

## Long-Term Strategy (Year 2+)

### Content Updates (Post-Launch)
- **Quarterly updates** with 3-5 new chapters
- **Seasonal events** with limited-time maps
- **New classes** via DLC ($0.99-1.99 each)
- **Character skins** as cosmetic DLC ($0.99)

### Community Building
- **Discord server** with 500+ members
- **Open source contributions** (accept community maps/classes)
- **Modding support** (custom campaign format)
- **Annual community tournament**

### Platform Expansion
- **iOS port** (expand market by 2-3x)
- **Steam release** (desktop version, $9.99)
- **Nintendo Switch** (if successful enough to justify port)

### Monetization Evolution
- **DLC packs**: $1.99-2.99 for 5-chapter story extensions
- **Soundtrack**: $2.99 for OST download
- **Cosmetic packs**: $0.99-1.99 for unit skins
- **No ads, no loot boxes, no gacha**

**Year 2 Revenue Projection:**
- 25K new downloads × $5 = $125K
- 5K DLC purchases × $2 = $10K
- **Total: $135K gross, $94.5K net**

---

## Resource Requirements

### Development Time
- **Phase 1-2**: 4 months (foundation + visuals)
- **Phase 3-4**: 4 months (audio + content)
- **Phase 5-6**: 4 months (polish + marketing)
- **Total: 12 months to launch**

### Budget Breakdown
| Category | Cost |
|----------|------|
| Art Assets (sprites, tiles, portraits) | $950-1,700 |
| Sound Effects | $200-400 |
| Background Music | $200-400 |
| Trailer Production | $300-500 |
| Google Play Developer Account | $25 (one-time) |
| Website Hosting | $10/month × 12 = $120 |
| Marketing/Advertising (optional) | $500-1,000 |
| **Total Estimated Budget** | **$2,295-4,145** |

### Skills Needed
- ✅ **Programming (Kotlin/Android)**: Already have
- ✅ **Game Design**: Core mechanics complete
- 🔄 **Pixel Art**: Commission or learn basic skills
- 🔄 **Audio Integration**: Learning required
- 🔄 **Level Design**: Can self-teach
- 🔄 **Marketing**: Basic skills needed

### Risk Mitigation
- **Budget constraint**: Start with cheaper assets, upgrade if successful
- **Time constraint**: Focus on MVP first, cut non-essential features
- **Solo development**: Document everything, engage community early
- **Market risk**: Soft launch to validate before full marketing spend

---

## Success Metrics & KPIs

### Development Phase
- ✅ On-time milestone delivery (80%+ met)
- ✅ Code quality maintained (0 critical bugs)
- ✅ Test coverage >25%
- ✅ Beta tester satisfaction >4/5 stars

### Launch Phase (First 30 Days)
- 🎯 **Downloads**: 1,000+ (organic)
- 🎯 **Revenue**: $3,500+ ($3.50 per download after Google cut)
- 🎯 **Rating**: 4.0+ stars with 50+ reviews
- 🎯 **Retention**: D7 >20%, D30 >10%
- 🎯 **Crash Rate**: <1% of sessions

### Growth Phase (First Year)
- 🎯 **Downloads**: 10,000+
- 🎯 **Revenue**: $35,000+
- 🎯 **Rating**: Maintain 4.0+ stars
- 🎯 **Community**: 500+ Discord members
- 🎯 **Updates**: 4 content updates delivered
- 🎯 **Break-even**: Cover all development costs

### Sustainability (Year 2+)
- 🎯 **Monthly downloads**: 1,000-2,000
- 🎯 **Monthly revenue**: $3,500-7,000
- 🎯 **Community growth**: 1,000+ Discord members
- 🎯 **Open source**: 5+ contributors
- 🎯 **Platform expansion**: iOS or Steam launch

---

## Decision Framework

### What to Build Next?
Use this priority framework:

**Priority 1: CRITICAL** (Blockers to launch)
- Missing features that prevent basic gameplay
- Critical bugs that cause crashes
- Performance issues on target devices

**Priority 2: HIGH** (Significantly impacts player experience)
- Visual polish (sprites, animations)
- Audio feedback
- Core campaign chapters (1-20)
- Tutorial

**Priority 3: MEDIUM** (Nice to have, improves experience)
- Additional difficulty modes
- Bonus chapters
- Achievement system
- Character viewer

**Priority 4: LOW** (Future content)
- Advanced features (class customization, skill trees)
- Platform expansion
- Multiplayer/PvP
- Modding tools

### When to Launch?
Launch when ALL of these are true:
- ✅ 15+ chapters with balanced difficulty
- ✅ All character classes have sprites
- ✅ Basic animations implemented
- ✅ Audio feedback on all actions
- ✅ Tutorial teaches core mechanics
- ✅ <5 critical bugs in testing
- ✅ 90% of testers rate 4+ stars
- ✅ Marketing assets ready (trailer, website)

**Don't launch if:**
- ❌ Campaign feels unfinished (<12 chapters)
- ❌ Game looks unpolished (text-only UI)
- ❌ Critical bugs remain
- ❌ Test player feedback is mixed/negative

---

## Next Steps (This Week)

### Immediate Actions
1. ✅ **Review this plan** - Get feedback from stakeholders
2. [ ] **Fix technical debt** - Clean up deprecation warnings
3. [ ] **Implement AI healing** - Complete existing TODO
4. [ ] **Create Phase 1 tasks** - Break down into GitHub issues
5. [ ] **Set up project board** - Kanban board for tracking

### This Month
1. [ ] Complete Phase 1 technical debt items
2. [ ] Design tutorial chapter
3. [ ] Research art asset options (commission vs create)
4. [ ] Set budget and timeline
5. [ ] Begin Phase 2 planning

### Commit to Timeline
- **Month 1-2**: Foundation Polish
- **Month 3-4**: Visual Transformation  
- **Month 5**: Audio & Feel
- **Month 6-8**: Content Creation
- **Month 9-10**: Polish & Testing
- **Month 11-12**: Marketing & Launch

**Target Launch Date: February 2027** (12 months from now)

---

## Conclusion

Open Tactics has everything needed to succeed: solid game engine, good architecture, and passionate development. The path to success is clear:

1. **Polish the visuals** - Transform from prototype to game
2. **Add audio** - Make it feel alive and engaging
3. **Create content** - Build a compelling 15-20 chapter campaign
4. **Test thoroughly** - Ensure quality and balance
5. **Market smartly** - Leverage open source and community
6. **Launch premium** - Respect players with honest pricing

This is achievable with focused effort over 12 months and a budget of $2-4K. The tactical RPG audience is there, waiting for a quality mobile game without predatory monetization.

**Let's build something players will love and recommend to their friends.**

---

*Last Updated: February 9, 2026*  
*Next Review: March 9, 2026 (after Phase 1 completion)*
