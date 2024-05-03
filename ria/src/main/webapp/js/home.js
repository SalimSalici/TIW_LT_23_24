{
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

				this.containerEl.appendChild(groupContainer);
			}
		}
	}

	function GroupDetails(_group, _groupEl, _groupDetailsContainer, _toggleBtnEl) {

		let self = this;

		this.group = _group;
		this.groupEl = _groupEl;
		this.groupDetailsContainer = _groupDetailsContainer;
		this.toggleBtnEl = _toggleBtnEl;

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
							self.expand(json.group, json.users);
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

		this.expand = function (group, users) {

			if (group.userId == loggedUser.id) {
				this.setupTrash(group);
			}

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
			for (let i = 0; i < users.length; i++) {
				let user = users[i];
				let userTile = this.userTileTemplate.cloneNode(true);
				userTile.removeAttribute("id");
				userTile.removeAttribute("style");
				userTile.setAttribute("data-memberid", user.id);

				let groupMemberUsername = userTile.querySelector("[data-templ=groupMemberUsername]");
				groupMemberUsername.innerText = user.username;

				let groupMemberName = userTile.querySelector("[data-templ=groupMemberName]");
				groupMemberName.innerText = user.name;

				let groupMemberSurname = userTile.querySelector("[data-templ=groupMemberSurname]");
				groupMemberSurname.innerText = user.surname;

				let userBoxTile = userTile.querySelector(".user-box-tile");
				userTile.addEventListener('dragstart', evt => {
					userBoxTile.classList.add("dragging");
					self.dragged = evt.target;
				});

				userTile.addEventListener('dragend', evt => {
					userBoxTile.classList.remove("dragging");
				});

				this.usersContainer.appendChild(userTile);
			}

			self.groupEl.classList.add("group-expanded");
			this.toggleBtnEl.disabled = false;
		}
		
		this.setupTrash = function(group) {
			this.usersTrashContainer.classList.remove("no-trash");
			
			this.trashBtn.addEventListener('dragover', function (evt) {
		        evt.preventDefault();  // Necessary to allow dropping
		    });
		    
			this.trashBtn.addEventListener("drop", evt => {
				
				// Client side check that minimum member requirment is satisfied after user removal
				if (self.group.userCount - 1 < self.group.minUsers) {
					createPopup("Group cannot have less than the current amount of members.", "error", 5000);
					return;
				}
				
				// Send request to server to remove the user
				let memberId = self.dragged.getAttribute("data-memberid");
				let formData = new FormData();
				formData.append("groupId", group.id);
				formData.append("memberId", memberId);
				makeCall("POST", "removegroupmember", formData, req => {
					if (req.readyState == 4) {
						switch (req.status) {
							case 200:
								createPopup("User removed from group successfully.", "success", "5000");
								self.dragged.parentElement.removeChild(self.dragged);
								self.group.userCount--;
								self.groupInfoMemeberCount.innerText = self.group.userCount;
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
									req.responseText : "Error removing user from group";
								createPopup(errorMsg, "error", 5000);
						}
					}
				})
			});
			
		}
	}

	let ownedGroupsManager = new GroupsManager("ownedactivegroups", document.getElementById("ownedActiveGroupsContainer"));
	ownedGroupsManager.fetchAndDisplayGroups();
	
	let otherGroupsManager = new GroupsManager("activegroups", document.getElementById("otherActiveGroupsContainer"));
	otherGroupsManager.fetchAndDisplayGroups();
}