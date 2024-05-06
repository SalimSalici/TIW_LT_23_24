{

	// TODO: Disable inputs when waiting for server response

	if (sessionStorage.getItem("user") == null)
		logout();

	function logout() {
		makeCall("POST", 'logout', null, function (risposta) {
			if (risposta.readyState == 4) {
				sessionStorage.removeItem("user");
				window.location.href = "auth.html";
			}
		});
	}

	document.querySelector("a[href='logout']").addEventListener('click', () => {
		sessionStorage.removeItem("user");
	});

	const loggedUser = JSON.parse(sessionStorage.getItem("user"));
	document.getElementById("welcomeNameText").innerText = loggedUser.username;

	let ownedGroupsManager = new GroupsManager("ownedactivegroups", document.getElementById("ownedActiveGroupsContainer"));
	ownedGroupsManager.fetchAndDisplayGroups();

	let otherGroupsManager = new GroupsManager("activegroups", document.getElementById("otherActiveGroupsContainer"));
	otherGroupsManager.fetchAndDisplayGroups();

	let createGroupManager = new CreateGroupManager();

	function CreateGroupManager() {
		let self = this;

		this.form = document.getElementById("createGroupForm");
		this.createGroupFieldset = document.getElementById("createGroupFieldset");
		this.errorBoxContainer = this.form.querySelector(".error-box");
		this.createGroupErrorList = document.getElementById("createGroupErrorList");
		this.groupNameInput = document.getElementById("groupNameInput");
		this.durationInput = document.getElementById("durationInput");
		this.minMembersInput = document.getElementById("minMembersInput");
		this.maxMembersInput = document.getElementById("maxMembersInput");
		this.goToUserSelectionBtn = document.getElementById("goToUserSelectionBtn");
		this.cancelGroupCreationBtn = document.getElementById("cancelGroupCreationBtn");
		this.modalContainer = document.getElementById("modalContainer");
		this.attemptText = document.getElementById("attemptText");
		this.inviteMemberListContainer = document.getElementById("inviteMemberListContainer");

		this.userCheckboxTemplate = document.getElementById("userCheckboxTemplate");

		this.attempt = 1;

		this.users = [];

		this.goToUserSelectionBtn.addEventListener("click", evt => {
			evt.preventDefault();
			evt.stopImmediatePropagation();
			evt.stopPropagation();

			self.hideErrors();

			if (self.form.checkValidity()) {
				if (parseInt(self.minMembersInput.value) > parseInt(self.maxMembersInput.value)) {
					createPopup("Minimum members must be less than or equal to the maximum members", "error", 5000);
					return;
				}

				self.createGroupFieldset.setAttribute("disabled", "true");
				makeCall("GET", "users", null, (req) => {
					if (req.readyState === 4) {
						switch (req.status) {
							case 200:
								self.users = JSON.parse(req.responseText);
								self.displayUserSelection();
								break;
							case 403:
								window.location.href = req.getResponseHeader("Location");
								window.sessionStorage.removeItem("user");
								break;
							default:
								let errorMsg = req.getResponseHeader('content-type').includes("application/json") ?
									req.responseText : "Error fetching all users.";
								createPopup(errorMsg, "error", 5000);
						}
						self.createGroupFieldset.removeAttribute("disabled");
					}
				});

			} else {
				self.form.reportValidity();
				createPopup("Please fill in all fields correctly", "error", 5000);
			}
		});

		this.form.addEventListener("submit", evt => {
			evt.preventDefault();
			evt.stopImmediatePropagation();
			evt.stopPropagation();

			if (self.form.checkValidity()) {
				let formData = new FormData(self.form);
				let usersCount = formData.getAll("usersToInvite[]").length;

				let errorMessage = null;
				let shortage = self.minMembersInput.value - usersCount;
				let excess = usersCount - self.maxMembersInput.value;
				if (shortage > 0) {
					errorMessage = "Not enough users selected. Select at least " + shortage + " more.";
				} else if (excess > 0) {
					errorMessage = "Too many users selected. Deselect at least " + excess + " user(s).";
				}

				if (errorMessage != null) {
					self.attempt++;
					self.attemptText.innerText = self.attempt;

					if (self.attempt <= 3) {
						createPopup(errorMessage, "error", 5000);
					} else {
						self.form.reset();
						self.closeModal();
						createPopup("Group creation cancelled because you exceeded 3 attempts. Please start over.", "error", 5000);
					}

					return;
				}

				self.createGroupFieldset.setAttribute("disabled", "true");
				makeCall("POST", "creategroup", formData, req => {
					if (req.readyState == 4) {
						switch (req.status) {
							case 200:
								ownedGroupsManager.fetchAndDisplayGroups();
								otherGroupsManager.fetchAndDisplayGroups();
								self.form.reset();
								self.closeModal();
								createPopup("Group created successfully.", "success", 5000);
								break;
							case 400:
								self.displayErrors(JSON.parse(req.responseText));
								self.closeModal();
								break;
							case 403:
								window.location.href = req.getResponseHeader("Location");
								window.sessionStorage.removeItem("user");
								break;
							default:
								let errorMsg = req.getResponseHeader('content-type').includes("application/json") ?
									req.responseText : "Error creating group.";
								createPopup(errorMsg, "error", 5000);
						}
						self.createGroupFieldset.removeAttribute("disabled");
					}
				})

			} else {
				self.form.reportValidity();
				self.closeModal();
				createPopup("Please fill in all fields correctly", "error", 5000);
			}
		})

		this.cancelGroupCreationBtn.addEventListener("click", evt => {
			self.form.reset();
			self.closeModal();
		});

		this.displayUserSelection = function () {
			self.attemptText.innerText = self.attempt = 1;
			self.inviteMemberListContainer.innerHTML = "";
			for (let i = 0; i < this.users.length; i++) {
				let user = self.users[i];
				let userCheckbox = self.userCheckboxTemplate.cloneNode(true);
				userCheckbox.removeAttribute("id");
				userCheckbox.removeAttribute("style");

				let userCheckboxInput = userCheckbox.querySelector("[name=usersToInvite\\[\\]]");
				userCheckboxInput.setAttribute("value", user.id);

				let name = userCheckbox.querySelector("[data-templ=name]");
				name.innerText = user.name;

				let surname = userCheckbox.querySelector("[data-templ=surname]");
				surname.innerText = user.surname;

				self.inviteMemberListContainer.appendChild(userCheckbox);
			}

			self.openModal();
		}

		this.displayErrors = function (errors) {
			self.createGroupErrorList.innerHTML = "";
			for (let i = 0; i < errors.length; i++) {
				let error = errors[i];
				let errorLi = document.createElement("li");
				errorLi.innerText = error;
				self.createGroupErrorList.appendChild(errorLi);
			}
			self.errorBoxContainer.style.display = "block";
		}

		this.hideErrors = function () {
			self.errorBoxContainer.style.display = "none";
		}

		this.openModal = function () {
			this.modalContainer.style.display = "block";
			setTimeout(() => {
				this.modalContainer.classList.remove("modal-hidden");
			}, 50);
		}

		this.closeModal = function () {
			this.modalContainer.classList.add("modal-hidden");
			setTimeout(() => {
				this.modalContainer.style.display = "none";
			}, 300);
		}
	}

	function GroupsManager(_url, _containerEl) {
		let self = this;

		this.url = _url;
		this.containerEl = _containerEl;
		this.groups = [];

		this.groupContainerTemplate = document.getElementById("groupContainerTemplate");

		this.fetchAndDisplayGroups = function () {
			makeCall("GET", this.url, null, (req) => {
				if (req.readyState == 4) {
					switch (req.status) {
						case 200:
							self.groups = JSON.parse(req.responseText);
							self.displayGroups();
							break;
						case 403:
							window.location.href = req.getResponseHeader("Location");
							window.sessionStorage.removeItem("user");
							break;
						default:
							createPopup("Error fetching owned groups", "error", 5000);
					}
				}
			});
		}

		this.displayGroups = function () {
			this.containerEl.innerHTML = "";
			for (let i = 0; i < this.groups.length; i++) {
				let group = this.groups[i];

				let groupContainer = this.groupContainerTemplate.cloneNode(true);
				groupContainer.removeAttribute("id");
				groupContainer.removeAttribute("style");

				let groupDetailsName = groupContainer.querySelector("[data-templ=groupDetailsName]");
				groupDetailsName.innerText = group.groupName;

				let groupPreviewDuration = groupContainer.querySelector("[data-templ=groupPreviewDuration]");
				groupPreviewDuration.innerText = group.duration;

				let groupPreviewMemberCount = groupContainer.querySelector("[data-templ=groupPreviewMemberCount]");
				groupPreviewMemberCount.innerText = group.userCount;

				let groupPreviewMaxUsers = groupContainer.querySelector("[data-templ=groupPreviewMaxUsers]");
				groupPreviewMaxUsers.innerText = group.maxUsers;

				let groupDetailsContainer = groupContainer.querySelector("[data-templ=groupDetailsContainer]");
				let toggleDetailsBtn = groupContainer.querySelector("[data-templ=toggleDetailsBtn]");

				let groupDetails = new GroupDetails(group, groupContainer, groupDetailsContainer, toggleDetailsBtn);

				group["containerEl"] = groupContainer;
				group["groupDetails"] = groupDetails;

				this.containerEl.appendChild(groupContainer);
			}
		}

		this.removeMemberFromGroup = function (groupId, memberId) {
			for (let i = 0; i < this.groups.length; i++) {
				if (this.groups[i].id == groupId) {
					let group = this.groups[i];
					group.groupDetails.removeUser(memberId);
					return;
				}
			}
		}
	}

	function GroupDetails(_group, _groupEl, _groupDetailsContainer, _toggleBtnEl) {

		let self = this;

		this.group = _group;
		this.groupEl = _groupEl;
		this.groupDetailsContainer = _groupDetailsContainer;
		this.toggleBtnEl = _toggleBtnEl;

		this.groupPreviewMemberCount = this.groupEl.querySelector("[data-templ=groupPreviewMemberCount]");

		this.groupInfoName = this.groupDetailsContainer.querySelector("[data-templ=groupInfoName]");
		this.groupInfoOwnerName = this.groupDetailsContainer.querySelector("[data-templ=groupInfoOwnerName]");
		this.groupInfoOwnerSurame = this.groupDetailsContainer.querySelector("[data-templ=groupInfoOwnerSurame]");
		this.groupInfoOwnerUsername = this.groupDetailsContainer.querySelector("[data-templ=groupInfoOwnerUsername]");
		this.groupInfoDuration = this.groupDetailsContainer.querySelector("[data-templ=groupInfoDuration]");
		this.groupInfoCreatedAt = this.groupDetailsContainer.querySelector("[data-templ=groupInfoCreatedAt]");
		this.groupInfoEndDate = this.groupDetailsContainer.querySelector("[data-templ=groupInfoEndDate]");
		this.groupInfoMemeberCount = this.groupDetailsContainer.querySelector("[data-templ=groupInfoMemeberCount]");
		this.groupInfoMinMembers = this.groupDetailsContainer.querySelector("[data-templ=groupInfoMinMembers]");
		this.groupInfoMaxMembers = this.groupDetailsContainer.querySelector("[data-templ=groupInfoMaxMembers]");

		this.usersTrashContainer = this.groupDetailsContainer.querySelector(".users-trash-container");

		this.usersContainer = this.groupDetailsContainer.querySelector(".users-container");
		this.trashBtn = this.groupDetailsContainer.querySelector(".trash-container");
		this.userTileTemplate = this.groupDetailsContainer.querySelector("[data-templ=userTileTemplate]");

		this.dragged = null;

		this.users = [];

		this.toggleBtnEl.addEventListener("click", evt => {
			if (self.groupEl.classList.contains("group-expanded")) { // close group details
				self.groupEl.classList.remove("group-expanded");
				self.usersContainer.innerHTML = "";
				self.toggleBtnEl.innerText = "View details";
			} else { // fetch group data and members and display
				self.toggleBtnEl.disabled = true;
				self.fetchAndExpand();
			}
		});

		this.fetchAndExpand = function () {
			let url = "groupdetails?id=" + this.group.id
			makeCall("GET", url, null, req => {
				if (req.readyState == 4) {
					switch (req.status) {
						case 200:
							let json = JSON.parse(req.responseText);
							self.toggleBtnEl.innerText = "Hide details";
							self.users = json.users;
							self.expand(json.group);
							break;
						case 403:
							window.location.href = req.getResponseHeader("Location");
							window.sessionStorage.removeItem("user");
							break;
						default:
							createPopup("Error fetching group", "error", 5000);
					}
				}

			});
		}

		this.expand = function (group) {

			// Group info
			this.groupInfoName.innerText = group.groupName;
			this.groupInfoOwnerName.innerText = group.owner.name;
			this.groupInfoOwnerSurame.innerText = group.owner.surname;
			this.groupInfoOwnerUsername.innerText = group.owner.username;
			this.groupInfoDuration.innerText = group.duration;
			this.groupInfoCreatedAt.innerText = group.createdAt;
			this.groupInfoEndDate.innerText = group.endDate;
			this.groupInfoMemeberCount.innerText = group.userCount;
			this.groupInfoMinMembers.innerText = group.minUsers;
			this.groupInfoMaxMembers.innerText = group.maxUsers;

			// Member list
			for (let i = 0; i < self.users.length; i++) {
				let user = self.users[i];
				let userTile = this.userTileTemplate.cloneNode(true);
				userTile.removeAttribute("id");
				userTile.removeAttribute("style");
				userTile.setAttribute("data-memberid", user.id);
				user["userTile"] = userTile;

				let groupMemberUsername = userTile.querySelector("[data-templ=groupMemberUsername]");
				groupMemberUsername.innerText = user.username;

				let groupMemberName = userTile.querySelector("[data-templ=groupMemberName]");
				groupMemberName.innerText = user.name;

				let groupMemberSurname = userTile.querySelector("[data-templ=groupMemberSurname]");
				groupMemberSurname.innerText = user.surname;

				let userBoxTile = userTile.querySelector(".user-box-tile");

				if (self.group.userId == loggedUser.id) {
					userTile.addEventListener('dragstart', evt => {
						userBoxTile.classList.add("dragging");
						self.dragged = evt.target;
					});

					userTile.addEventListener('dragend', evt => {
						userBoxTile.classList.remove("dragging");
					});
				} else {
					userTile.removeAttribute("draggable");
				}

				this.usersContainer.appendChild(userTile);
			}

			self.groupEl.classList.add("group-expanded");
			this.toggleBtnEl.disabled = false;
		}

		this.setupTrash = function () {
			this.usersTrashContainer.classList.remove("no-trash");

			this.trashBtn.addEventListener('dragover', function (evt) {
				evt.preventDefault();  // Necessary to allow dropping
			});

			this.trashBtn.addEventListener("drop", evt => {

				if (self.dragged === null) {
					createPopup("Please, drop a valid user of this group.", "error", 5000);
					return;
				}

				// Client side check that minimum member requirment is satisfied after user removal
				if (self.group.userCount - 1 < self.group.minUsers) {
					createPopup("Group cannot have less than the current amount of members.", "error", 5000);
					return;
				}

				// Send request to server to remove the user
				let memberId = self.dragged.getAttribute("data-memberid");
				let formData = new FormData();
				formData.append("groupId", self.group.id);
				formData.append("memberId", memberId);
				makeCall("POST", "removegroupmember", formData, req => {
					if (req.readyState == 4) {
						switch (req.status) {
							case 200:
								createPopup("User removed from group successfully.", "success", "5000");
								// self.dragged.parentElement.removeChild(self.dragged);
								// self.group.userCount--;
								// self.groupInfoMemeberCount.innerText = self.group.userCount;
								// self.removeUser(memberId);
								otherGroupsManager.removeMemberFromGroup(self.group.id, memberId);
								ownedGroupsManager.removeMemberFromGroup(self.group.id, memberId);
								break;
							case 409:
								createPopup("Group cannot have less than the current amount of members.", "error", 5000);
								break;
							case 403:
								window.location.href = req.getResponseHeader("Location");
								window.sessionStorage.removeItem("user");
								break;
							default:
								let errorMsg = req.getResponseHeader('content-type').includes("application/json") ?
									req.responseText : "Error removing user from group.";
								createPopup(errorMsg, "error", 5000);
						}
					}
				})
			});
		}

		this.removeUser = function (userId) {
			self.group.userCount--;
			self.groupInfoMemeberCount.innerText = self.group.userCount;
			self.groupPreviewMemberCount.innerText = self.group.userCount;
			for (let i = 0; i < self.users.length; i++) {
				if (self.users[i].id == userId) {
					let user = self.users[i];
					self.users.splice(i, 1);
					if (self.usersContainer.contains(user.userTile))
						self.usersContainer.removeChild(user.userTile);
					return;
				}
			}
		}

		if (this.group.userId == loggedUser.id) {
			this.setupTrash();
		}
	}
}