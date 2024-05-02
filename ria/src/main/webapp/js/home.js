{
	function GroupsManager(_url, _containerEl) {
		let self = this;
		
		this.url = _url;
		this.containerEl = _containerEl;
		this.groups = [];
		
		this.groupContainerTemplate = document.getElementById("groupContainerTemplate");
		
		this.fetchAndDisplayGroups = function() {
			makeCall("GET", this.url, null, (req) => {
				if (req.readyState == 4) {
					switch (req.status) {
						case 200:
							self.groups = JSON.parse(req.responseText);
							self.displayGroups();
							break;
						default:
							createPopup("Error fetching owned groups", "error", 5000);
					}
				}
			});
		}
		
		this.displayGroups = function() {
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
				
				let groupDetails = new GroupDetails(group.id, groupContainer, groupDetailsContainer, toggleDetailsBtn);
				
				this.containerEl.appendChild(groupContainer);
			}
		}
	}

	function GroupDetails(_groupId, _groupEl, _groupDetailsContainer, _toggleBtnEl) {

		let self = this;

		this.groupId = _groupId;
		this.groupEl = _groupEl;
		this.groupDetailsContainer = _groupDetailsContainer;
		this.toggleBtnEl = _toggleBtnEl;
		
		this.usersContainer = this.groupDetailsContainer.querySelector(".users-container");
		
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

		this.toggleBtnEl.addEventListener("click", evt => {
			if (self.groupEl.classList.contains("group-expanded")) { // close group details
				self.groupEl.classList.remove("group-expanded");
			} else { // fetch group data and members and display
				this.toggleBtnEl.disabled = true;
				self.fetchAndExpand();
			}
		});

		this.fetchAndExpand = function() {
			let url = "groupdetails?id=" + this.groupId
			makeCall("GET", url, null, req => {
				if (req.readyState == 4) {
					switch (req.status) {
						case 200:
							let json = JSON.parse(req.responseText);
							self.expand(json.group, json.users);
							break;
							
					}
				}
					
			});
		}
		
		this.expand = function(group, users) {
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

			self.groupEl.classList.add("group-expanded");
			this.toggleBtnEl.disabled = false;
		}
	}
	
	let ownedGroupsManager = new GroupsManager("ownedactivegroups", document.getElementById("ownedActiveGroupsContainer"));
	ownedGroupsManager.fetchAndDisplayGroups();
}