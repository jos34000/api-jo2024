## [1.3.1](https://github.com/jos34000/api-jo2024/compare/v1.3.0...v1.3.1) (2026-04-17)


### Bug Fixes

* remove expiry date check from ticket scan ([7b5e518](https://github.com/jos34000/api-jo2024/commit/7b5e518a059c58560188018a73ff932a6f7567c0))

# [1.3.0](https://github.com/jos34000/api-jo2024/compare/v1.2.0...v1.3.0) (2026-04-17)


### Features

* [DJ-114] - Add admin sales datas ([e7db069](https://github.com/jos34000/api-jo2024/commit/e7db0693fe280ae6e4d6af17dc91150edf0a2640))

# [1.2.0](https://github.com/jos34000/api-jo2024/compare/v1.1.0...v1.2.0) (2026-04-17)


### Features

* [DJ-110] - Scan response ([314e4d5](https://github.com/jos34000/api-jo2024/commit/314e4d52ae6829e4da54c47f9d06780be9ed2153))
* [DJ-282] - Can login as a staff member ([c93830d](https://github.com/jos34000/api-jo2024/commit/c93830d4c2d174b7364b46cbde41ec15edf9eab1))

# [1.1.0](https://github.com/jos34000/api-jo2024/compare/v1.0.0...v1.1.0) (2026-04-17)


### Features

* **test:** expand service layer coverage with JaCoCo enforcement ([c6990ed](https://github.com/jos34000/api-jo2024/commit/c6990eda55b03de944844b456c828f87d268d02b))

# [1.0.0](https://github.com/jos34000/api-jo2024/compare/v0.10.0...v1.0.0) (2026-03-30)


### Features

* **test:** Implement test for non regression ([f9f450d](https://github.com/jos34000/api-jo2024/commit/f9f450d51b8c7bf26616d871424ec896a0de4d32))


### BREAKING CHANGES

* **test:** Add comprehensive non-regression test suite covering all service layers

# [0.10.0](https://github.com/jos34000/api-jo2024/compare/v0.9.1...v0.10.0) (2026-03-29)


### Bug Fixes

* **locale:** use Locale.of() for Java 21, clarify from() contract in test ([a731022](https://github.com/jos34000/api-jo2024/commit/a731022785384920b9c4daa269d75e44bea38069))
* **security:** replace wildcards with per-method matchers, fix Transactional import in UserService ([97f5d8d](https://github.com/jos34000/api-jo2024/commit/97f5d8d3fb2418f0732903875661f0c906dce561))


### Features

* **admin:** Add admin actions ([98ec5b6](https://github.com/jos34000/api-jo2024/commit/98ec5b6297f6f0cabbe6bffb35dc809ef6ad2d5d))
* **cart:** add expireIfNeeded() domain method to Cart entity ([4dc68cb](https://github.com/jos34000/api-jo2024/commit/4dc68cbb44e2bf23dd48ba5c821fe6f08808f1c1))
* **cart:** add ICartService interface ([43c5a51](https://github.com/jos34000/api-jo2024/commit/43c5a5181b2814a4ede7562b964d42170ec09c54))
* **checkout:** add ICheckoutService interface ([3c89dc6](https://github.com/jos34000/api-jo2024/commit/3c89dc66f3fcf9c1888103390d06e134e8efe73a))
* **db:** add used column and index to password_reset_token ([7c9d0ea](https://github.com/jos34000/api-jo2024/commit/7c9d0eaf28e2e0f53d5ef5e2cac068817d3a166d))
* **dto:** add Update DTOs for event, offer, sport, user role ([af82437](https://github.com/jos34000/api-jo2024/commit/af82437b996ae435a3bfe82bf7190245b29e9006))
* **event:** add update and delete admin endpoints ([c7981d5](https://github.com/jos34000/api-jo2024/commit/c7981d50cb07accceaea4810156dd20300d5896d))
* **locale:** add SupportedLocale enum with from() factory ([9a65d6a](https://github.com/jos34000/api-jo2024/commit/9a65d6ad0c42b841ae794ec5b8dba7d7b98ca983))
* **offer:** add update and delete admin endpoints ([0edcc17](https://github.com/jos34000/api-jo2024/commit/0edcc17523efd76617d5249cca5ae401b9e96e20))
* **payment:** add PaymentResult record and PaymentGateway interface ([0423264](https://github.com/jos34000/api-jo2024/commit/042326445bc3844afcb4187f296cd7b8df93398a))
* **payment:** implement MockPaymentGateway wrapping PaymentMockService ([430adf3](https://github.com/jos34000/api-jo2024/commit/430adf3de3c52e5790173ab73788f6c58bdcd5c1))
* **sport:** add update and delete admin endpoints ([b2d41c2](https://github.com/jos34000/api-jo2024/commit/b2d41c2e1cc596fff99c6081e258e9bcf3b9d3e8))
* **token:** add ResetTokenStore interface and HmacResetTokenStore ([f732e7e](https://github.com/jos34000/api-jo2024/commit/f732e7e2266f0b8e3bd586f8fe20c4d2f6c272c6))
* **token:** add used field to PasswordResetToken, add findByHashedToken query ([d6b6f58](https://github.com/jos34000/api-jo2024/commit/d6b6f58e60cfdf70538167a9b17167eb5fdc5139))
* **user:** add admin endpoints — list all, delete, update role ([b8e59f2](https://github.com/jos34000/api-jo2024/commit/b8e59f210bb435379427811644696168b8215a8b))
* **user:** expose id in UserResponseDTO ([7c8dced](https://github.com/jos34000/api-jo2024/commit/7c8dced186d9753d198cc6be99927ca1aa5fd2d8))

## [0.9.1](https://github.com/jos34000/api-jo2024/compare/v0.9.0...v0.9.1) (2026-03-22)


### Bug Fixes

* **workflow:** Add docker variables ([87eb67e](https://github.com/jos34000/api-jo2024/commit/87eb67ecdb012f117c9384e1dc840a5257005722))

# [0.9.0](https://github.com/jos34000/api-jo2024/compare/v0.8.0...v0.9.0) (2026-03-22)


### Features

* **docker:** Generate & push images on release ([c195817](https://github.com/jos34000/api-jo2024/commit/c1958179bdd62d37ecedb4946c03d2e3a58185c6))

# [0.8.0](https://github.com/jos34000/api-jo2024/compare/v0.7.0...v0.8.0) (2026-03-22)


### Bug Fixes

* **events:** Translate descriptions of events ([244a2a8](https://github.com/jos34000/api-jo2024/commit/244a2a89eee5b9187a30a49ec47d39b7a3d7eb34))
* **reservation:** Decrement available seats on checkout ([f614a20](https://github.com/jos34000/api-jo2024/commit/f614a208c3141373d67edc6a3eac798386982cc2))


### Features

* [DJ-107] - Add translation to persisted datas ([07303ca](https://github.com/jos34000/api-jo2024/commit/07303cabe2a36ac035dd8079e66ade7400694dd4))
* **events:** add more events & translations ([acd8510](https://github.com/jos34000/api-jo2024/commit/acd85107b694b717285c2294e5bfcb7c4c14ee8e))
* **tickets:** Translate mail & pdf contents ([a810504](https://github.com/jos34000/api-jo2024/commit/a810504bc8c4ddf2df6e6945c4dc70418ff28faf))

# [0.7.0](https://github.com/jos34000/api-jo2024/compare/v0.6.0...v0.7.0) (2026-03-20)


### Features

* [DJ-101] - Download directly tickets ([726977c](https://github.com/jos34000/api-jo2024/commit/726977caca955e61441ef6058ffed03f9305960c))
* [DJ-102] - Consult history of commands ([ff82a10](https://github.com/jos34000/api-jo2024/commit/ff82a10980041e5ff2f75d09b56dd162276ca14c))
* [DJ-98] - Proceed to payment ([709f2df](https://github.com/jos34000/api-jo2024/commit/709f2df63342a664768b94798f01705ed9b5df38))
* [DJ-99] - Reveice e-tickets by mail ([be557bf](https://github.com/jos34000/api-jo2024/commit/be557bf4c0bdfbcc27912f148df16609b663531a))

# [0.6.0](https://github.com/jos34000/api-jo2024/compare/v0.5.0...v0.6.0) (2026-03-15)


### Bug Fixes

* **error:** Clarify thrown error messages ([dd30d68](https://github.com/jos34000/api-jo2024/commit/dd30d68d9592898f8107ca556b4433878a177ca7))


### Features

* [DJ-92] - Possibility to remove cart items ([40a2fbd](https://github.com/jos34000/api-jo2024/commit/40a2fbdd835de0d9132e68de9ad56eddf7cbf39a))
* [DJ-96] - Clear cartItems in a cart ([e3adc5c](https://github.com/jos34000/api-jo2024/commit/e3adc5cabf02f2844e8f68f2ed6139737794f536))
* **cart:** Possibility to increase/decrease offer ([a5207d6](https://github.com/jos34000/api-jo2024/commit/a5207d6469ddcae55e3b7082de62ef74268e83f8))

# [0.5.0](https://github.com/jos34000/api-jo2024/compare/v0.4.0...v0.5.0) (2026-03-14)


### Bug Fixes

* **offers:** return ID in DTO for offers ([e1c7a1d](https://github.com/jos34000/api-jo2024/commit/e1c7a1d6fe5f515f5dadf4224ef88c33e0fcdc5a))


### Features

* [DJ-92] - Add items to cart ([090dc90](https://github.com/jos34000/api-jo2024/commit/090dc9081df4be5980f7f3258ff8d6121bb8441a))
* [DJ-92] - Implement add to cart feature ([1d3e9cc](https://github.com/jos34000/api-jo2024/commit/1d3e9cc64f31f244cb915b3cf4ad3db00f3accb5))

# [0.4.0](https://github.com/jos34000/api-jo2024/compare/v0.3.0...v0.4.0) (2026-03-13)


### Features

* [DJ-93] - Add cart GET endpoint and refactor offer package ([0c6f59e](https://github.com/jos34000/api-jo2024/commit/0c6f59e69a6f5888b5d61330d5a6ca86dd47c750))
* [DJ-93] - Add cart GET endpoint and refactor offer package ([830b87f](https://github.com/jos34000/api-jo2024/commit/830b87f2861aadc3c93df231f4aad36a2dccde2b))

# [0.3.0](https://github.com/jos34000/api-jo2024/compare/v0.2.0...v0.3.0) (2026-03-13)


### Features

* [Sprint 3] - Core features ([e2e51f9](https://github.com/jos34000/api-jo2024/commit/e2e51f94c3fe23310055a44bf4a6cbb51973fdb6))

# [0.2.0](https://github.com/jos34000/api-jo2024/compare/v0.1.0...v0.2.0) (2026-02-21)


### Features

* [Sprint 2] - Add auth features ([f310dd5](https://github.com/jos34000/api-jo2024/commit/f310dd52ca409ab7146e6c47fd4e21b21126ab24))

# [0.1.0](https://github.com/jos34000/api-jo2024/compare/v0.0.0...v0.1.0) (2026-01-27)


### Features

* [Sprint 1] - Core infrastructure and security setup ([f395b43](https://github.com/jos34000/api-jo2024/commit/f395b43c00e3c102a27d91de4ea94ec5ce43b918))
